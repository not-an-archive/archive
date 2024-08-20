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
            id,
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
              id,
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

      def read(id: PID): IO[Result[Organisation]] =
        sql"""
          SELECT
            id,
            name
          FROM
            organisations
          WHERE
            id = $id
        """
        .query[Organisation]
        .option
        .transact(transactor)
        .map:
          case Some(organisation) => Right(organisation)
          case None => Left(NotFoundError(name, id))

      def delete(id: PID): IO[Result[Unit]] =
        sql"""
          DELETE FROM
            organisations
          WHERE
            id = $id
        """
        .update
        .run
        .transact(transactor)
        .map(expectUpdate(id))

      def update(organisation: Organisation): IO[Result[Unit]] =
        sql"""
          UPDATE
            organisations
          SET
            name = ${organisation.name}
          WHERE
            id = ${organisation.pid}
        """
        .update
        .run
        .transact(transactor)
        .map(expectUpdate(organisation.pid))

      private def expectUpdate(id: Option[PID])(rowCount: Int): Result[Unit] =
        id match
          case None                      => Left(NoPIDError(name))
          case Some(id) if rowCount == 0 => Left(UpdateError(name, id))
          case _                         => Right(())

      private def expectUpdate(id: PID)(rowCount: Int): Result[Unit] =
        expectUpdate(Some(id))(rowCount)

  given uuidMeta: Meta[UUID] =
    import UUID.compat.*
    doobie.h2.implicits.UuidType.imap(_.asScalaUUID)(_.asJavaUUID)
