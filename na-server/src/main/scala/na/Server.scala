package na

import cats.data.*
import cats.effect.*
import cats.implicits.*

import doobie.hikari.HikariTransactor
import doobie.util.*

import fs2.*

import org.http4s.implicits.*
import org.http4s.ember.server.*
import org.http4s.server.middleware.*

import na.*
import organisation.*

object Server extends IOApp:

  case class Environment(transactor: HikariTransactor[IO], config: Config)

  def environment: Resource[IO, Environment] =
    for {
      config     <- Config.load
      dbec       <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Archive.transactor(config.database, dbec)
    } yield Environment(transactor, config)

  def create: IO[ExitCode] =
    environment.use(instantiate)

  private def instantiate(environment: Environment): IO[ExitCode] =
    for {
      _          <- Archive.initialize(environment.transactor)
      repository =  OrganisationRepository(environment.transactor)
      httpApp    =  ErrorHandling.Recover.total(
                      ErrorAction.log(
                        OrganisationService(repository).http,
                        messageFailureLogAction = errorHandler,
                        serviceErrorLogAction   = errorHandler
                      )
                    )
      exitCode   <- EmberServerBuilder
                      .default[IO]
                      .withHost(environment.config.server.host)
                      .withPort(environment.config.server.port)
                      .withHttpApp(httpApp.orNotFound)
                      .build
                      .use(_ => IO.never)
                      .as(ExitCode.Success)
    } yield exitCode

  def run(args: List[String]): IO[ExitCode] =
    println(s"args=${args.foreach(println)}")
    create

  private def errorHandler(t: Throwable, msg: => String) : OptionT[IO, Unit] =
    OptionT.liftF(IO.println(msg) >> IO.println(t) >> IO(t.printStackTrace()))
