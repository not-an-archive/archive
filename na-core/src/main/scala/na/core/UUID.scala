package na
package core

import io.circe.*
import io.circe.generic.semiauto.*
import na.core.UUID.compat.JavaUUID
import org.http4s.Uri.Path.SegmentEncoder

case class UUID(msb: Long, lsb: Long):

  import UUID.*

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

  def transform(v: Version): UUID =
    copy(msb = (msb & ~Version.Mask) | (v.msb & Version.Mask))

  override def toString: String =
    JavaUUID(msb, lsb).toString

object UUID:

  import compat.*

  def fromString(s: String): UUID =
    JavaUUID.fromString(s).asScalaUUID

  given organisationEncoder: Encoder[UUID] =
    Encoder.encodeUUID.contramap(_.asJavaUUID)

  given organisationDecoder: Decoder[UUID] =
    Decoder.decodeUUID.map(_.asScalaUUID)

  given segmentEncoder: SegmentEncoder[UUID] =
    SegmentEncoder.stringSegmentEncoder.contramap[UUID](_.toString)

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
    case Unused                      extends Version(0x00000000_0000_0000_00L)
    case GregorianTimeBased          extends Version(0x00000000_0000_0010_00L)
    case DCESecurityBased            extends Version(0x00000000_0000_0020_00L)
    case MD5HashNameBased            extends Version(0x00000000_0000_0030_00L)
    case RandomGeneratedBased        extends Version(0x00000000_0000_0040_00L)
    case SHA1HashNameBased           extends Version(0x00000000_0000_0050_00L)
    case ReorderedGregorianTimeBased extends Version(0x00000000_0000_0060_00L)
    case UnixEpochTimeBased          extends Version(0x00000000_0000_0070_00L)
    case CustomFormatBased           extends Version(0x00000000_0000_0080_00L)
    case Version9                    extends Version(0x00000000_0000_0090_00L)
    case Version10                   extends Version(0x00000000_0000_00A0_00L)
    case Version11                   extends Version(0x00000000_0000_00B0_00L)
    case Version12                   extends Version(0x00000000_0000_00C0_00L)
    case Version13                   extends Version(0x00000000_0000_00D0_00L)
    case Version14                   extends Version(0x00000000_0000_00E0_00L)
    case Version15                   extends Version(0x00000000_0000_00F0_00L)

  object Version:

    val Mask: Long = 0x00000000_0000_00F0_00L

    def fromMostSignificantBits(msb: Long): Version =
      Version
        .values
        .find(_.msb == (msb & Mask))
        .getOrElse(sys.error(s"missing version definition for version bits ${msb & Mask}"))


  object compat:

    type JavaUUID = java.util.UUID

    object JavaUUID:

      def fromString(name: String): JavaUUID =
        java.util.UUID.fromString(name)

      def apply(msb: Long, lsb: Long): JavaUUID =
        java.util.UUID.apply(msb, lsb)

      def randomUUID: JavaUUID =
        java.util.UUID.randomUUID

      def nameUUIDFromBytes(bytes: Array[Byte]): JavaUUID =
        java.util.UUID.nameUUIDFromBytes(bytes)

    extension (uuid: UUID) def asJavaUUID: JavaUUID =
      JavaUUID(uuid.msb, uuid.lsb)

    extension (javaUUID: JavaUUID) def asScalaUUID: UUID =
      UUID(javaUUID.getMostSignificantBits, javaUUID.getLeastSignificantBits)
