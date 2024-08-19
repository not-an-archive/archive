package na
package core

case class UUID(msb: Long, lsb: Long):

  import UUID.*
  import Variant.*
  import Version.*

  /**
   * The variant field determines the overall layout of the UUID.  That is, the interpretation of all other bits in the
   * UUID depends on the setting of the bits in the variant field.  As such, it could more accurately be called a type
   * field; we retain the original term for compatibility.  The variant field consists of a variable number of the most
   * significant bits of the first three bits of octet 8 of the UUID, which is in the least significant bits part.
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
    val firstThreeBits = (lsb >>> 61) & 0x0000_0000_0000_0007
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
   * accurately be called a sub type field.  The version field consists of the most significant four bits of octet 10 of
   * the UUID, which is in the most significant bits part.
   *
   * The following table lists the currently-defined versions.
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
  lazy val version: Option[Version] =
    Version.values.find(v => (msb & v.mask) == v.bits)

enum Variant:
  case NCSBackwardsCompatible
  case LeachSalz
  case MicrosoftBackwardsCompatible
  case Reserved


enum Version(val bits: Long):
  val mask: Long = 0x0000_0000_0000_f000L
  case GregorianTimeBased          extends Version(0x0000_0000_0000_1000L)
  case DCESecurityBased            extends Version(0x0000_0000_0000_2000L)
  case MD5HashNameBased            extends Version(0x0000_0000_0000_3000L)
  case RandomGeneratedBased        extends Version(0x0000_0000_0000_4000L)
  case SHA1HashNameBased           extends Version(0x0000_0000_0000_5000L)
  case ReorderedGregorianTimeBased extends Version(0x0000_0000_0000_6000L)
  case UnixEpochTimeBased          extends Version(0x0000_0000_0000_7000L)
  case CustomFormatBased           extends Version(0x0000_0000_0000_8000L)

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