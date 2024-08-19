package na
package core

import org.scalacheck.*

object JavaUUIDCompatibilityProps extends Properties("uuid.compat"):

  import generators.*
  import Prop.*

  property("applyIsJavaUUIDCompatible") =
    forAll(javaApplyUUIDs)(isJavaUUIDCompatible)

  property("v4IsJavaUUIDCompatible - RandomBased") =
    forAll(javaVersion4UUIDs)(isJavaUUIDVersion4Compatible)

  property("v3IsJavaUUIDCompatible - MD5HashBased") =
    forAll(javaVersion3UUIDs)(isJavaUUIDVersion3Compatible)

  import Variant.*
  import Version.*
  
  import compat.*
  import compat.JavaUUID.*

  def isJavaUUIDCompatible(javaUUID: JavaUUID): Boolean =
    isJavaUUIDVersionCompatible(javaUUID) && isJavaUUIDVariantCompatible(javaUUID)

  def isJavaUUIDVariantCompatible(javaUUID: JavaUUID): Boolean =
    javaUUID.variant match
      case 0 => javaUUID.asScala.variant == NCSBackwardsCompatible
      case 2 => javaUUID.asScala.variant == LeachSalz
      case 6 => javaUUID.asScala.variant == MicrosoftBackwardsCompatible
      case 7 => javaUUID.asScala.variant == Reserved
      case _ => false

  def isJavaUUIDVersionCompatible(javaUUID: JavaUUID): Boolean =
    javaUUID.version match
      case  0 => javaUUID.asScala.version == Unused
      case  1 => javaUUID.asScala.version == GregorianTimeBased
      case  2 => javaUUID.asScala.version == DCESecurityBased
      case  3 => javaUUID.asScala.version == MD5HashNameBased
      case  4 => javaUUID.asScala.version == RandomGeneratedBased
      case  5 => javaUUID.asScala.version == SHA1HashNameBased
      case  6 => javaUUID.asScala.version == ReorderedGregorianTimeBased
      case  7 => javaUUID.asScala.version == UnixEpochTimeBased
      case  8 => javaUUID.asScala.version == CustomFormatBased
      case  9 => javaUUID.asScala.version == Version9
      case 10 => javaUUID.asScala.version == Version10
      case 11 => javaUUID.asScala.version == Version11
      case 12 => javaUUID.asScala.version == Version12
      case 13 => javaUUID.asScala.version == Version13
      case 14 => javaUUID.asScala.version == Version14
      case 15 => javaUUID.asScala.version == Version15
      case _  => false

  def isJavaUUIDVersion4Compatible(javaUUID: JavaUUID): Boolean =
    val isRandomBased = javaUUID.asScala.version == RandomGeneratedBased
    isRandomBased && isJavaUUIDVariantCompatible(javaUUID)

  def isJavaUUIDVersion3Compatible(javaUUID: JavaUUID, name: Array[Byte]): Boolean =
    val isNameBased = java.util.UUID.nameUUIDFromBytes(name) == javaUUID
    val isMD5Hased  = javaUUID.asScala.version == MD5HashNameBased
    isNameBased && isMD5Hased && isJavaUUIDVariantCompatible(javaUUID)


  object generators:

    import Arbitrary.*

    val javaApplyUUIDs: Gen[JavaUUID] =
      for {
        msb <- arbitrary[Long]
        lsb <- arbitrary[Long]
      } yield JavaUUID(msb, lsb)

    val javaVersion4UUIDs: Gen[JavaUUID] =    
      Gen.map(_ => JavaUUID.randomUUID)

    val javaVersion3UUIDs: Gen[(JavaUUID,Array[Byte])] =    
      Gen
        .containerOf[Array,Byte](arbitrary[Byte])
        .map(bytes => (JavaUUID.nameUUIDFromBytes(bytes), bytes))
