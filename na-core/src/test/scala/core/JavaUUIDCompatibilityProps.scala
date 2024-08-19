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
    isJavaUUIDVersionCompatible(javaUUID) &&
    isJavaUUIDVariantCompatible(javaUUID)

  def isJavaUUIDVariantCompatible(javaUUID: JavaUUID): Boolean =
    javaUUID.variant match
      case 0 => javaUUID.asScala.variant == NCSBackwardsCompatible
      case 2 => javaUUID.asScala.variant == LeachSalz
      case 6 => javaUUID.asScala.variant == MicrosoftBackwardsCompatible
      case 7 => javaUUID.asScala.variant == Reserved
      case _ => false

  def isJavaUUIDVersionCompatible(javaUUID: JavaUUID): Boolean =
    javaUUID.version match
      case 1 => javaUUID.asScala.version.contains(GregorianTimeBased)
      case 2 => javaUUID.asScala.version.contains(DCESecurityBased)
      case 3 => javaUUID.asScala.version.contains(MD5HashNameBased)
      case 4 => javaUUID.asScala.version.contains(RandomGeneratedBased)
      case 5 => javaUUID.asScala.version.contains(SHA1HashNameBased)
      case 6 => javaUUID.asScala.version.contains(ReorderedGregorianTimeBased)
      case 7 => javaUUID.asScala.version.contains(UnixEpochTimeBased)
      case 8 => javaUUID.asScala.version.contains(CustomFormatBased)
      case _ => javaUUID.asScala.version.isEmpty

  def isJavaUUIDVersion4Compatible(javaUUID: JavaUUID): Boolean =
    val isRandomBased = javaUUID.asScala.version.contains(RandomGeneratedBased)
    isRandomBased && isJavaUUIDVariantCompatible(javaUUID)

  def isJavaUUIDVersion3Compatible(javaUUID: JavaUUID, name: Array[Byte]): Boolean =
    val isNameBased = java.util.UUID.nameUUIDFromBytes(name) == javaUUID
    val isMD5Hased  = javaUUID.asScala.version.contains(MD5HashNameBased)
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
