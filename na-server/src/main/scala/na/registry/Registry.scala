package na
package registry

import core.*

import cats.effect.IO

import fs2.Stream



object Registry:

  enum Result:
    /** Successfully registered given external PID */
    /** Duplicate PID found. Returned if copies or a differently born instance of given PID exist */
    case Registered
    case Duplicate

  import java.util.UUID

  import doobie.Meta
  import doobie.implicits.*
  import doobie.util.transactor.*

  import PID.*

  given Meta[UUID] = doobie.h2.implicits.UuidType

  given Meta[Born] = Meta.IntMeta.imap(Born.fromOrdinal)(_.ordinal)

  given Meta[Copy] = Meta.IntMeta.imap(Copy.fromOrdinal)(_.ordinal)

  def apply(transactor: Transactor[IO]): Registry =
    new Registry:

      def register(pid: PID): IO[Result] =
        ???

      def create(pid: PID): IO[Unit] =
        val ext = pid.asExternal
        sql"""
          INSERT INTO pid (
            uuid,
            ext_msb,
            ext_lsb,
            born,
            copy
          )
          VALUES (
            ${pid.toUUID},
            ${ext.msb},
            ${ext.lsb},
            ${ext.born},
            ${ext.copy}
          )
        """
          .update
          .run
          .transact(transactor)
          .void

      def stream: Stream[IO, PID] =
        sql"""
          SELECT
            uuid
          FROM
            pid
        """
          .query[PID]
          .stream
          .transact(transactor)

// interface

abstract class Registry:
  import Registry.*
  def register(pid: PID): IO[Result]
  def stream: Stream[IO,PID]
