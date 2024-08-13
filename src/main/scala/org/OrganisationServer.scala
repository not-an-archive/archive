package org

import cats.effect.*
import cats.implicits.*
import fs2.*

import org.http4s.implicits.*
import org.http4s.ember.server.*

import doobie.util.*
import doobie.hikari.HikariTransactor

import na.nl.*


object OrganisationServer extends IOApp:

  def create: IO[ExitCode] =
    resources.use(instantiate)

  def resources: Resource[IO, Resources] =
    for {
      config     <- Config.load
      ec         <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Database.transactor(config.database)(ec)
    } yield Resources(transactor, config)

  def instantiate(resources: Resources): IO[ExitCode] =
    for {
      _          <- Database.initialize(resources.transactor)
      repository =  OrganisationRepository(resources.transactor)
      exitCode   <- EmberServerBuilder
                      .default[IO]
                      .withHost(resources.config.server.host)
                      .withPort(resources.config.server.port)
                      .withHttpApp(OrganisationService(repository).http.orNotFound)
                      .build
                      .use(_ => IO.never)
                      .as(ExitCode.Success)
    } yield exitCode

  case class Resources(transactor: HikariTransactor[IO], config: Config)

  def run(args: List[String]): IO[ExitCode] =
    create
