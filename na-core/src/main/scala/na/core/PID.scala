package na
package core

import io.circe.*
import io.circe.generic.semiauto.*

import UUID.*
import Variant.*
import Version.*

case class PID(uuid: UUID):
  assert(uuid.variant == LeachSalz, "No archive compliant uuid variant: LeachSalz)")
  assert(uuid.version == CustomFormatBased, "no archive compliant uuid version: CustomFormatBased")

object PID:

  import compat.*
  import UUID.compat.*

  def fromString(s: String): PID =
    UUID.fromString(s).toPID

  def random(digitallyBorn: Boolean): PID =
    val uuid = JavaUUID.randomUUID.asScalaUUID.transform(CustomFormatBased)

    if digitallyBorn then
      uuid.copy(lsb = uuid.lsb.set(Born.Digitally.mask)).toPID
    else
      uuid.copy(lsb = uuid.lsb.unset(Born.Physically.mask)).toPID

  enum Born(val mask: Long, lsb: Long):
    case Digitally  extends Born(mask = 0x00_0000_000000000001L, lsb = 0x00_0000_000000000000L)
    case Physically extends Born(mask = 0x00_0000_000000000001L, lsb = 0x00_0000_000000000001L)

  enum Copy(mask: Long, lsb: Long):
    case External   extends Copy(mask = 0x00_0000_000000000006L, lsb = 0x00_0000_000000000000L)
    case Internal1  extends Copy(mask = 0x00_0000_000000000006L, lsb = 0x00_0000_000000000002L)
    case Internal2  extends Copy(mask = 0x00_0000_000000000006L, lsb = 0x00_0000_000000000005L)
    case Internal3  extends Copy(mask = 0x00_0000_000000000006L, lsb = 0x00_0000_000000000006L)

  given organisationEncoder: Encoder[PID] =
    Encoder.encodeUUID.contramap(_.uuid.asJavaUUID)

  given organisationDecoder: Decoder[PID] =
    Decoder.decodeUUID.map(_.asScalaUUID.toPID)

  object compat:

    extension (uuid: UUID) def toPID: PID =
      PID(uuid)

    extension (bits: Long) def set(mask: Long): Long =
      bits | mask

    extension (bits: Long) def unset(mask: Long): Long =
      bits & ~mask