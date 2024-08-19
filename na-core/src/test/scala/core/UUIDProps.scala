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
      case Unused                      =>  (msb & 0xF000L) == 0x0000L
      case GregorianTimeBased          =>  (msb & 0xF000L) == 0x1000L
      case DCESecurityBased            =>  (msb & 0xF000L) == 0x2000L
      case MD5HashNameBased            =>  (msb & 0xF000L) == 0x3000L
      case RandomGeneratedBased        =>  (msb & 0xF000L) == 0x4000L
      case SHA1HashNameBased           =>  (msb & 0xF000L) == 0x5000L
      case ReorderedGregorianTimeBased =>  (msb & 0xF000L) == 0x6000L
      case UnixEpochTimeBased          =>  (msb & 0xF000L) == 0x7000L
      case CustomFormatBased           =>  (msb & 0xF000L) == 0x8000L
      case Version9                    =>  (msb & 0xF000L) == 0x9000L
      case Version10                   =>  (msb & 0xF000L) == 0xA000L
      case Version11                   =>  (msb & 0xF000L) == 0xB000L
      case Version12                   =>  (msb & 0xF000L) == 0xC000L
      case Version13                   =>  (msb & 0xF000L) == 0xD000L
      case Version14                   =>  (msb & 0xF000L) == 0xE000L
      case Version15                   =>  (msb & 0xF000L) == 0xF000L
  }
