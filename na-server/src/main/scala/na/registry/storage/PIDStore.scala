package na.registry.storage

import cats.effect.*
import fs2.*

import na.core.*

abstract class PIDStore:
  import doobie.*
  /** creates given PIDs */
  def create(pid: PID): ConnectionIO[Int]
  /** streams all _external_ PIDs */
  def streamExt: Stream[ConnectionIO,PID]
  /** streams all _external_ and _internal_ PIDs */
  def streamAll: Stream[ConnectionIO,PID]

object PIDStore:

  import java.util.UUID

  import doobie.*
  import doobie.implicits.*
  import PID.*

  given Meta[UUID] =
    doobie.h2.implicits.UuidType // H2 dependency ???

  given Meta[Born] =
    Meta.IntMeta.imap(Born.fromOrdinal)(_.ordinal)

  given Meta[Copy] =
    Meta.IntMeta.imap(Copy.fromOrdinal)(_.ordinal)

  def apply(transactor: Transactor[IO]): PIDStore =
    new PIDStore:

      def create(pid: PID): ConnectionIO[Int] =
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
            ${pid.born},
            ${pid.copy}
          )
        """
          .update
          .run

      def streamExt: Stream[ConnectionIO,PID] =
        sql"""
          SELECT
            ext_msb,
            ext_lsb
          FROM
            pid
        """
          .query[PID]
          .stream


      def streamAll: Stream[ConnectionIO,PID] =
        sql"""
          SELECT
            uuid
          FROM
            pid
        """
          .query[PID]
          .stream
