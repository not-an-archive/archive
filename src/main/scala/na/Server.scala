package na

import cats.effect.*
import cats.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.*
import fs2.*
import na.*
import org.http4s.ember.server.*
import org.http4s.implicits.*

import organisation.*

object Server extends IOApp:

  case class Environment(transactor: HikariTransactor[IO], config: Config)

  private def environment: Resource[IO, Environment] =
    for {
      config <- Config.load
      ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Archive.transactor(config.database, ec)
    } yield Environment(transactor, config)

  def create: IO[ExitCode] =
    environment.use(instantiate)

  private def instantiate(environment: Environment): IO[ExitCode] =
    for {
      _          <- Archive.initialize(environment.transactor)
      repository =  OrganisationRepository(environment.transactor)
      exitCode   <- EmberServerBuilder
                      .default[IO]
                      .withHost(environment.config.server.host)
                      .withPort(environment.config.server.port)
                      .withHttpApp(OrganisationService(repository).http.orNotFound)
                      .build
                      .use(_ => IO.never)
                      .as(ExitCode.Success)
    } yield exitCode

  def run(args: List[String]): IO[ExitCode] =
    create
