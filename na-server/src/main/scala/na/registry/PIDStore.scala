package na
package registry
package store

import cats.data.*

import fs2.*

import core.*

abstract class PIDStore:

  import doobie.*

  /** creates given _external_ PID */
  def create(pid: PID): ConnectionIO[Int]
  /** get registration for given _external_ PID */
  def get(pid: PID): ConnectionIO[Registration]
  /** streams all _external_ PIDs */
  def streamExt: Stream[ConnectionIO,PID]
  /** streams all _external_ and _internal_ PIDs */
  def streamAll: Stream[ConnectionIO,PID]

object PIDStore extends PIDStore:

  import java.util.UUID

  import doobie.*
  import doobie.implicits.*
  import PID.*
  import Copy.*

  case class RegistrationRow(uuid: UUID, externalMsb: Long, externalLsb: Long, born: Born, copy: Copy):
    lazy val externalPID: PID = PID(externalMsb, externalLsb)
    lazy val internalPID: PID = uuid.toPID

  def fromRows(rows: NonEmptyList[RegistrationRow]): Registration =
    val copies = rows.foldLeft(Map.empty[Copy, PID])((acc, row) => acc + (row.copy -> row.uuid.toPID))
    Registration(copies)


  given Meta[UUID] =
    doobie.h2.implicits.UuidType // TODO H2 dependency ???

  given Meta[Born] =
    Meta.IntMeta.imap(Born.fromOrdinal)(_.ordinal)

  given Meta[Copy] =
    Meta.IntMeta.imap(Copy.fromOrdinal)(_.ordinal)

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

  def get(pid: PID): ConnectionIO[Registration] =
    sql"""
      SELECT
        uuid,
        ext_msb,
        ext_lsb,
        born,
        copy
      FROM
        pid
      WHERE
        ext_msb = ${pid.msb} AND
        ext_lsb = ${pid.lsb}
    """
      .query[RegistrationRow]
      .nel
      .map(fromRows)


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


