package na
package core

import io.circe.*

import java.util.*

case class PID(msb: Long, lsb: Long):

  import PID.*

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
    import Variant.*
    val firstThreeBits = (lsb & 0xE000_000000000000L) >>> 61
    firstThreeBits match
      case 0 => NCSBackwardsCompatible
      case 1 => NCSBackwardsCompatible
      case 2 => NCSBackwardsCompatible
      case 3 => NCSBackwardsCompatible
      case 4 => LeachSalz
      case 5 => LeachSalz
      case 6 => MicrosoftBackwardsCompatible
      case 7 => Reserved


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
   */
  lazy val version: Version =
    Version.values.find(extractor(msb)).getOrElse(sys.error(s"no version embedding: msb=${msb.toBinaryString}"))

  lazy val born: Born =
    Born.values.find(extractor(lsb)).getOrElse(sys.error(s"no born embedding: lsb=${lsb.toBinaryString}"))

  lazy val copy: Copy =
    Copy.values.find(extractor(lsb)).getOrElse(sys.error(s"no copy embedding: lsb=${lsb.toBinaryString}"))

  private def extractor(bits: Long)(v: Embedded): Boolean =
    v.value == (bits & v.mask)

  def embed(value: Embedded, significant: Boolean): PID =
    if significant then
      copy(msb = (msb & ~value.mask) | (value.value & value.mask))
    else
      copy(lsb = (lsb & ~value.mask) | (value.value & value.mask))

  override def toString: String =
    UUID(msb, lsb).toString


object PID:

  def fromString(s: String): PID =
    UUID.fromString(s).toPID

  def random(born: Born, copy: Copy): PID =
    UUID
      .randomUUID
      .toPID
      .embed(Version.CustomFormatBased, significant = true)
      .embed(born, significant = false)
      .embed(copy, significant = false)

  abstract class Embedded(val mask: Long):
    val value: Long

  enum Variant(override val value: Long) extends Embedded(mask = 0xE000_000000000000L):
    case NCSBackwardsCompatible       extends Variant(value = 0x0000_000000000000L)
    case LeachSalz                    extends Variant(value = 0x8000_000000000000L)
    case MicrosoftBackwardsCompatible extends Variant(value = 0xC000_000000000000L)
    case Reserved                     extends Variant(value = 0xE000_000000000000L)

  enum Version(override val value: Long) extends Embedded(mask = 0x00000000_0000_F000L):
    case Unused                      extends Version(value = 0x00000000_0000_0000L)
    case GregorianTimeBased          extends Version(value = 0x00000000_0000_1000L)
    case DCESecurityBased            extends Version(value = 0x00000000_0000_2000L)
    case MD5HashNameBased            extends Version(value = 0x00000000_0000_3000L)
    case RandomGeneratedBased        extends Version(value = 0x00000000_0000_4000L)
    case SHA1HashNameBased           extends Version(value = 0x00000000_0000_5000L)
    case ReorderedGregorianTimeBased extends Version(value = 0x00000000_0000_6000L)
    case UnixEpochTimeBased          extends Version(value = 0x00000000_0000_7000L)
    case CustomFormatBased           extends Version(value = 0x00000000_0000_8000L)
    case Version9                    extends Version(value = 0x00000000_0000_9000L)
    case Version10                   extends Version(value = 0x00000000_0000_A000L)
    case Version11                   extends Version(value = 0x00000000_0000_B000L)
    case Version12                   extends Version(value = 0x00000000_0000_C000L)
    case Version13                   extends Version(value = 0x00000000_0000_D000L)
    case Version14                   extends Version(value = 0x00000000_0000_E000L)
    case Version15                   extends Version(value = 0x00000000_0000_F000L)

  /** An ordinal persisted enumeration of whether the PID's information is either born digitally or physically */
  enum Born(override val value: Long) extends Embedded(mask = 0x0000_000000000001L):
    case Digitally  extends Born(value = 0x0000_000000000000L)
    case Physically extends Born(value = 0x0000_000000000001L)

  /** An ordinal persisted enumeration of whether the PID's information is known externally or copied internally. */
  enum Copy(override val value: Long) extends Embedded(mask = 0x0000_000000000006L):
    case External   extends Copy(value = 0x0000_000000000000L)
    case Internal1  extends Copy(value = 0x0000_000000000002L)
    case Internal2  extends Copy(value = 0x0000_000000000004L)
    case Internal3  extends Copy(value = 0x0000_000000000006L)


  given pidEncoder: Encoder[PID] =
    Encoder.encodeUUID.contramap(_.toUUID)

  given pidDecoder: Decoder[PID] =
    Decoder.decodeUUID.map(_.toPID)

  import org.http4s.Uri.Path.SegmentEncoder

  given pidSegmentEncoder: SegmentEncoder[PID] =
    SegmentEncoder.uuidSegmentEncoder.contramap[PID](_.toUUID)


  extension (bits: Long) def set(mask: Long): Long =
    bits | mask

  extension (bits: Long) def unset(mask: Long): Long =
    bits & ~mask

  extension (pid: PID) def toUUID: UUID =
    UUID(pid.msb, pid.lsb)

  extension (uuid: UUID) def toPID: PID =
    PID(uuid.getMostSignificantBits, uuid.getLeastSignificantBits)
    
  extension (pid: PID) def asExternal: PID =
    pid.embed(Copy.External, significant = false)
