package na
package core

import org.scalacheck.*

object PIDProps_UUID extends Properties("na.core.PID.UUID"):

  import java.util.*

  import generators.*
  import Prop.*

  import PID.*

  property("applyIsJavaUUIDCompatible") =
    forAll(javaApplyUUIDs)(isJavaUUIDCompatible)

  property("v4IsJavaUUIDCompatible - RandomBased") =
    forAll(javaVersion4UUIDs)(isJavaUUIDVersion4Compatible)

  property("v3IsJavaUUIDCompatible - MD5HashBased") =
    forAll(javaVersion3UUIDs)(isJavaUUIDVersion3Compatible)

  def isJavaUUIDCompatible(uuid: UUID): Boolean =
    isJavaUUIDVersionCompatible(uuid) && isJavaUUIDVariantCompatible(uuid)

  def isJavaUUIDVariantCompatible(uuid: UUID): Boolean =
    import Variant.*
    uuid.variant match
      case 0 => uuid.toPID.variant == NCSBackwardsCompatible
      case 2 => uuid.toPID.variant == LeachSalz
      case 6 => uuid.toPID.variant == MicrosoftBackwardsCompatible
      case 7 => uuid.toPID.variant == Reserved
      case _ => false

  def isJavaUUIDVersionCompatible(uuid: UUID): Boolean =
    import Version.*
    uuid.version match
      case  0 => uuid.toPID.version == Unused
      case  1 => uuid.toPID.version == GregorianTimeBased
      case  2 => uuid.toPID.version == DCESecurityBased
      case  3 => uuid.toPID.version == MD5HashNameBased
      case  4 => uuid.toPID.version == RandomGeneratedBased
      case  5 => uuid.toPID.version == SHA1HashNameBased
      case  6 => uuid.toPID.version == ReorderedGregorianTimeBased
      case  7 => uuid.toPID.version == UnixEpochTimeBased
      case  8 => uuid.toPID.version == CustomFormatBased
      case  9 => uuid.toPID.version == Version9
      case 10 => uuid.toPID.version == Version10
      case 11 => uuid.toPID.version == Version11
      case 12 => uuid.toPID.version == Version12
      case 13 => uuid.toPID.version == Version13
      case 14 => uuid.toPID.version == Version14
      case 15 => uuid.toPID.version == Version15
      case _  => false

  def isJavaUUIDVersion4Compatible(uuid: UUID): Boolean =
    val isRandomBased = uuid.toPID.version == Version.RandomGeneratedBased
    isRandomBased && isJavaUUIDVariantCompatible(uuid)

  def isJavaUUIDVersion3Compatible(uuid: UUID, name: Array[Byte]): Boolean =
    val isNameBased = UUID.nameUUIDFromBytes(name) == uuid
    val isMD5Hased  = uuid.toPID.version == Version.MD5HashNameBased
    isNameBased && isMD5Hased && isJavaUUIDVariantCompatible(uuid)


  object generators:

    import Arbitrary.*

    val javaApplyUUIDs: Gen[UUID] =
      for {
        msb <- arbitrary[Long]
        lsb <- arbitrary[Long]
      } yield UUID(msb, lsb)

    val javaVersion4UUIDs: Gen[UUID] =    
      Gen.map(_ => UUID.randomUUID)

    val javaVersion3UUIDs: Gen[(UUID, Array[Byte])] =    
      Gen
        .containerOf[Array,Byte](arbitrary[Byte])
        .map(bytes => (UUID.nameUUIDFromBytes(bytes), bytes))
