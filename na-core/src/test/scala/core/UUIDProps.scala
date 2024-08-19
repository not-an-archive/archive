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
      case Some(TimeBased)        =>  (msb & 0xF000L) == 0x1000L
      case Some(DCESecurityBased) =>  (msb & 0xF000L) == 0x2000L
      case Some(MD5HashBased)     =>  (msb & 0xF000L) == 0x3000L
      case Some(RandomBased)      =>  (msb & 0xF000L) == 0x4000L
      case Some(SHA1HashBased)    =>  (msb & 0xF000L) == 0x5000L
      case Some(Version6)         =>  (msb & 0xF000L) == 0x6000L
      case Some(Version7)         =>  (msb & 0xF000L) == 0x7000L
      case Some(Version8)         =>  (msb & 0xF000L) == 0x8000L
      case None                   => ((msb & 0xF000L) == 0x0000L) || ((msb & 0xF000L) >= 0x7000L)
  }
