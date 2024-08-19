package na
package core

import org.scalacheck.*

object UUIDProps extends Properties("na.core.UUID"):

  import core.*

  import util.*
  import Prop.*
  import Variant.*
  import Version.*

  property("variant") = forAll { (msb: Long, lsb: Long) =>
    UUID(msb, lsb).variant match
      case Reserved                     => lsb.toBinaryString.startsWith("111")
      case MicrosoftBackwardsCompatible => lsb.toBinaryString.startsWith("110")
      case LeachSalz                    => lsb.toBinaryString.startsWith("10")
      case NCSBackwardsCompatible       => true
  }

  property("version") = forAll { (msb: Long, lsb: Long) =>
    UUID(msb, lsb).version match
      case Some(GregorianTimeBased)          =>  (msb & 0xf000L) == 0x1000L
      case Some(DCESecurityBased)            =>  (msb & 0xf000L) == 0x2000L
      case Some(MD5HashNameBased)            =>  (msb & 0xf000L) == 0x3000L
      case Some(RandomGeneratedBased)        =>  (msb & 0xf000L) == 0x4000L
      case Some(SHA1HashNameBased)           =>  (msb & 0xf000L) == 0x5000L
      case Some(ReorderedGregorianTimeBased) =>  (msb & 0xf000L) == 0x6000L
      case Some(UnixEpochTimeBased)          =>  (msb & 0xf000L) == 0x7000L
      case Some(CustomFormatBased)           =>  (msb & 0xf000L) == 0x8000L
      case None                              => ((msb & 0xf000L) == 0x0000L) || ((msb & 0xf000L) >= 0x8000L)
  }
