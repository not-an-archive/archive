package naa

import scala.concurrent.*

import cats.effect.*

import doobie.*
import doobie.util.log.LogEvent
import doobie.hikari.HikariTransactor

import org.flywaydb.core.*

object Archive:

  private val printSqlLogHandler: LogHandler[IO] =
    (logEvent: LogEvent) => IO.delay(println(logEvent.sql))

  def transactor(config: DatabaseConfig, ec: ExecutionContext): Resource[IO, HikariTransactor[IO]] =
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
