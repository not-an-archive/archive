package organisation

import cats.implicits.*
import cats.effect.*
import fs2.*
import doobie.Meta
import doobie.implicits.*
import doobie.util.transactor.*

import na.*
import core.*

object OrganisationRepository:

  def apply(transactor: Transactor[IO]): Repository[IO, Organisation] =
    new Repository[IO, Organisation]("organisations"):

      def stream: Stream[IO, Organisation] =
        sql"""
          SELECT
            pid,
            name
          FROM
            organisations
        """
        .query[Organisation]
        .stream
        .transact(transactor)

      def create(organisation: Organisation): IO[Result[Unit]] =
        sql"""
          INSERT INTO organisations (
              pid,
              name
          )
          VALUES (
            ${organisation.pid},
            ${organisation.name}
          )
        """
        .update
        .run
        .transact(transactor)
        .map(expectUpdate(organisation.pid))

      def read(pid: PID): IO[Result[Organisation]] =
        sql"""
          SELECT
            pid,
            name
          FROM
            organisations
          WHERE
            pid = $pid
        """
        .query[Organisation]
        .option
        .transact(transactor)
        .map:
          case Some(organisation) => Right(organisation)
          case None => Left(NotFoundError(name, pid))

      def delete(pid: PID): IO[Result[Unit]] =
        sql"""
          DELETE FROM
            organisations
          WHERE
            pid = $pid
        """
        .update
        .run
        .transact(transactor)
        .map(expectUpdate(pid))

      def update(organisation: Organisation): IO[Result[Unit]] =
        sql"""
          UPDATE
            organisations
          SET
            name = ${organisation.name}
          WHERE
            pid = ${organisation.pid}
        """
        .update
        .run
        .transact(transactor)
        .map(expectUpdate(organisation.pid))

      private def expectUpdate(pid: Option[PID])(rowCount: Int): Result[Unit] =
        pid match
          case None                       => Left(NoPIDError(name))
          case Some(pid) if rowCount == 0 => Left(UpdateError(name, pid))
          case _                          => Right(())

      private def expectUpdate(pid: PID)(rowCount: Int): Result[Unit] =
        expectUpdate(Some(pid))(rowCount)

  given uuidMeta: Meta[UUID] =
    import UUID.compat.*
    doobie.h2.implicits.UuidType.imap(_.asScalaUUID)(_.asJavaUUID)
