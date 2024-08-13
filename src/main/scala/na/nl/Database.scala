package na.nl

import cats.effect.*
import org.flywaydb.core.*
import scala.concurrent.*

object Database:

  import doobie.*
  import doobie.util.log.LogEvent
  import doobie.hikari.HikariTransactor

  val printSqlLogHandler: LogHandler[IO] =
    (logEvent: LogEvent) => IO.delay(println(logEvent.sql))

  def transactor(config: DatabaseConfig)(implicit ec: ExecutionContext): Resource[IO, HikariTransactor[IO]] =
    HikariTransactor
      .newHikariTransactor[IO](
        driverClassName = config.driver,
        url             = config.url,
        user            = config.user,
        pass            = config.password,
        connectEC       = ec,
        logHandler      = None // Some(printSqlLogHandler)
      )

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] =
    transactor.configure: datasource =>
      IO.delay:
        Flyway
          .configure()
          .dataSource(datasource)
          .load()
          .migrate()
