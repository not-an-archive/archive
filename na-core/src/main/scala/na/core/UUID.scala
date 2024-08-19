package na
package core

case class UUID(msb: Long, lsb: Long):

  import UUID.*
  import Variant.*
  import Version.*

  lazy val variant: Variant =
    (lsb >>> 61) & 0x0000_0000_0000_0007 match
      case 0x00 => NCSBackwardsCompatible
      case 0x01 => NCSBackwardsCompatible
      case 0x02 => NCSBackwardsCompatible
      case 0x03 => NCSBackwardsCompatible
      case 0x04 => LeachSalz
      case 0x05 => LeachSalz
      case 0x06 => MicrosoftBackwardsCompatible
      case 0x07 => Reserved

  lazy val version: Option[Version] =
    (msb >>> 12) & 0x0000_0000_0000_000f match
      case 0x01 => Some(TimeBased)
      case 0x02 => Some(DCESecurityBased)
      case 0x03 => Some(MD5HashBased)
      case 0x04 => Some(RandomBased)
      case 0x05 => Some(SHA1HashBased)
      case 0x06 => Some(Version6)
      case 0x07 => Some(Version7)
      case 0x08 => Some(Version8)
      case _    => None

enum Variant(val bits: Long):
  val mask: Long = 0xeffff_ffff_ffff_fffL
  case NCSBackwardsCompatible       extends Variant(0x2111_1111_1111_1111L)
  case LeachSalz                    extends Variant(0x5111_1111_1111_1111L)
  case MicrosoftBackwardsCompatible extends Variant(0xD111_1111_1111_1111L)
  case Reserved                     extends Variant(0xF111_1111_1111_1111L)


enum Version(val bits: Long):
  val mask: Long = 0xffff_ffff_ffff_0fffL
  case TimeBased        extends Version(0x0000_0000_0000_1000L)
  case DCESecurityBased extends Version(0x0000_0000_0000_2000L)
  case MD5HashBased     extends Version(0x0000_0000_0000_3000L)
  case RandomBased      extends Version(0x0000_0000_0000_4000L)
  case SHA1HashBased    extends Version(0x0000_0000_0000_5000L)
  case Version6         extends Version(0x0000_0000_0000_6000L)
  case Version7         extends Version(0x0000_0000_0000_7000L)
  case Version8         extends Version(0x0000_0000_0000_8000L)

object compat:

  type JavaUUID = java.util.UUID

  object JavaUUID:
    def apply(msb: Long, lsb: Long): JavaUUID =
      java.util.UUID.apply(msb, lsb)

    def randomUUID: JavaUUID =
      java.util.UUID.randomUUID

    def nameUUIDFromBytes(bytes: Array[Byte]): JavaUUID =
      java.util.UUID.nameUUIDFromBytes(bytes)

    extension (uuid: UUID) def asJavaUUID: JavaUUID =
      JavaUUID(uuid.msb, uuid.lsb)

    extension (javaUUID: JavaUUID) def asScala: UUID =
      UUID(javaUUID.getMostSignificantBits, javaUUID.getLeastSignificantBits)