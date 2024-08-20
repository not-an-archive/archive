package na
package core

import io.circe.*

case class PID(msb: Long, lsb: Long):

  import java.util.*

  import PID.*

//  assert(variant == Variant.LeachSalz, "No archive compliant uuid variant: LeachSalz)")
//  assert(version == Version.CustomFormatBased, "no archive compliant uuid version: CustomFormatBased")
//
  /**
   * The variant field determines the overall layout of the UUID.  That is, the interpretation of all other bits in the
   * UUID depends on the setting of the bits in the variant field.  As such, it could more accurately be called a type
   * field; we retain the original term for compatibility.  The variant field consists of a variable number of bits from
   * the first three bits of octet 8 of the UUID, which is in the least significant bits part.
   *
   * The following table lists the contents of the variant field, where the letter "x" indicates a "don't-care" value.
   *
   * Msb0  Msb1  Msb2  Description
   * 0     x     x     Reserved, NCS backward compatibility.
   * 1     0     x     LeachSalz, the variant specified in this UUID implementation by all version constructors.
   * 1     1     0     Reserved, Microsoft Corporation backward compatibility.
   * 1     1     1     Reserved for future definition.
   */
  lazy val variant: Variant =
    Variant.fromLeastSignificantBits(lsb)

  /**
   * The version field determines the specific layout of the UUID.  That is, the interpretation of all other bits in the
   * UUID depends on the setting of the bits in the version field within a certain variant.  As such, it could more
   * accurately be called a sub type field.  The version field consists of a the first four bits of octet 10 of the
   * UUID, which is in the most significant bits part.
   *
   * The following table lists the contents of the version field.
   *
   * Msb0	Msb1	Msb2	Msb3	Version	Description
   * 0	  0	    0	    0	    0	      Unused
   * 0	  0	    0	    1	    1	      The Gregorian time-based UUID from in RFC4122, Section 4.1.3
   * 0	  0	    1	    0	    2	      DCE Security version, with embedded POSIX UIDs from RFC4122, Section 4.1.3
   * 0	  0	    1	    1	    3	      The name-based version specified in RFC4122, Section 4.1.3 that uses MD5 hashing.
   * 0	  1	    0	    0	    4	      The randomly generated version specified in RFC4122, Section 4.1.3.
   * 0	  1	    0	    1	    5	      The name-based version specified in RFC4122, Section 4.1.3 that uses SHA-1 hashing.
   * 0	  1	    1	    0	    6	      Reordered Gregorian time-based UUID specified in this document.
   * 0	  1	    1	    1	    7	      Unix Epoch time-based UUID specified in this document.
   * 1	  0	    0	    0	    8	      Reserved for custom UUID formats specified in this document.
   * 1	  0	    0	    1	    9	      Reserved for future definition.
   * 1	  0	    1	    0	    10	    Reserved for future definition.
   * 1	  0	    1	    1	    11	    Reserved for future definition.
   * 1	  1	    0	    0	    12	    Reserved for future definition.
   * 1	  1	    0	    1	    13	    Reserved for future definition.
   * 1	  1	    1	    0	    14	    Reserved for future definition.
   * 1	  1	    1	    1	    15	    Reserved for future definition.
   *
   * Unused or those versions reserved for future definition are modelled as [None] for future compatibility reasons.
   */
  lazy val version: Version =
    Version.fromMostSignificantBits(msb)

  def transform(v: Version): PID =
    copy(msb = (msb & ~Version.Mask) | (v.msb & Version.Mask))

  override def toString: String =
    UUID(msb, lsb).toString


object PID:

  import java.util.*

  def fromString(s: String): PID =
    UUID.fromString(s).toPID

  def random(digitallyBorn: Boolean): PID =
    val pid: PID =
      UUID
        .randomUUID
        .toPID
        .transform(Version.CustomFormatBased)

    if digitallyBorn then
      pid.copy(lsb = pid.lsb.set(Born.Digitally.mask))
    else
      pid.copy(lsb = pid.lsb.unset(Born.Physically.mask))

  enum Variant:
    case NCSBackwardsCompatible
    case LeachSalz
    case MicrosoftBackwardsCompatible
    case Reserved

  object Variant:
    def fromLeastSignificantBits(lsb: Long): Variant =
      val firstThreeBits = (lsb >>> 61) & 0x00_0000_000000000007L
      firstThreeBits match
        case 0 => NCSBackwardsCompatible
        case 1 => NCSBackwardsCompatible
        case 2 => NCSBackwardsCompatible
        case 3 => NCSBackwardsCompatible
        case 4 => LeachSalz
        case 5 => LeachSalz
        case 6 => MicrosoftBackwardsCompatible
        case 7 => Reserved

  enum Version(val msb: Long):
    case Unused extends Version(0x00000000_0000_0000_00L)
    case GregorianTimeBased extends Version(0x00000000_0000_0010_00L)
    case DCESecurityBased extends Version(0x00000000_0000_0020_00L)
    case MD5HashNameBased extends Version(0x00000000_0000_0030_00L)
    case RandomGeneratedBased extends Version(0x00000000_0000_0040_00L)
    case SHA1HashNameBased extends Version(0x00000000_0000_0050_00L)
    case ReorderedGregorianTimeBased extends Version(0x00000000_0000_0060_00L)
    case UnixEpochTimeBased extends Version(0x00000000_0000_0070_00L)
    case CustomFormatBased extends Version(0x00000000_0000_0080_00L)
    case Version9 extends Version(0x00000000_0000_0090_00L)
    case Version10 extends Version(0x00000000_0000_00A0_00L)
    case Version11 extends Version(0x00000000_0000_00B0_00L)
    case Version12 extends Version(0x00000000_0000_00C0_00L)
    case Version13 extends Version(0x00000000_0000_00D0_00L)
    case Version14 extends Version(0x00000000_0000_00E0_00L)
    case Version15 extends Version(0x00000000_0000_00F0_00L)

  object Version:
    val Mask: Long = 0x00000000_0000_00F0_00L

    def fromMostSignificantBits(msb: Long): Version =
      Version
        .values
        .find(_.msb == (msb & Mask))
        .getOrElse(sys.error(s"missing version definition for version bits ${msb & Mask}"))

  enum Born(val mask: Long, lsb: Long):
    case Digitally  extends Born(mask = 0x00_0000_000000000001L, lsb = 0x00_0000_000000000000L)
    case Physically extends Born(mask = 0x00_0000_000000000001L, lsb = 0x00_0000_000000000001L)

  enum Copy(mask: Long, lsb: Long):
    case External   extends Copy(mask = 0x00_0000_000000000006L, lsb = 0x00_0000_000000000000L)
    case Internal1  extends Copy(mask = 0x00_0000_000000000006L, lsb = 0x00_0000_000000000002L)
    case Internal2  extends Copy(mask = 0x00_0000_000000000006L, lsb = 0x00_0000_000000000005L)
    case Internal3  extends Copy(mask = 0x00_0000_000000000006L, lsb = 0x00_0000_000000000006L)

  import org.http4s.Uri.Path.SegmentEncoder

  given organisationEncoder: Encoder[PID] =
    Encoder.encodeUUID.contramap(_.toUUID)

  given organisationDecoder: Decoder[PID] =
    Decoder.decodeUUID.map(_.toPID)

  given segmentEncoder: SegmentEncoder[PID] =
    SegmentEncoder.uuidSegmentEncoder.contramap[PID](_.toUUID)


  extension (bits: Long) def set(mask: Long): Long =
    bits | mask

  extension (bits: Long) def unset(mask: Long): Long =
    bits & ~mask

  extension (pid: PID) def toUUID: UUID =
    UUID(pid.msb, pid.lsb)

  extension (uuid: UUID) def toPID: PID =
    PID(uuid.getMostSignificantBits, uuid.getLeastSignificantBits)
