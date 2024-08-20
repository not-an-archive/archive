package na
package core

import org.scalacheck.*

import PID.*

object PIDProps extends Properties("na.core.PID"):

  import util.*
  import Prop.*

  property("variant") = forAll { (msb: Long, lsb: Long) =>
    import Variant.*
    PID(msb, lsb).variant match
      case Reserved                     => lsb.toBinaryString.startsWith("111")
      case MicrosoftBackwardsCompatible => lsb.toBinaryString.startsWith("110")
      case LeachSalz                    => lsb.toBinaryString.startsWith("10")
      case NCSBackwardsCompatible       => true
  }

  property("version") = forAll { (msb: Long, lsb: Long) =>
    import Version.*
    val expected = maskedValue(0x00000000_0000_00F0_00L)
    PID(msb, lsb).version match
      case Unused                      =>  expected(msb) == 0x00000000_0000_0000_00L
      case GregorianTimeBased          =>  expected(msb) == 0x00000000_0000_0010_00L
      case DCESecurityBased            =>  expected(msb) == 0x00000000_0000_0020_00L
      case MD5HashNameBased            =>  expected(msb) == 0x00000000_0000_0030_00L
      case RandomGeneratedBased        =>  expected(msb) == 0x00000000_0000_0040_00L
      case SHA1HashNameBased           =>  expected(msb) == 0x00000000_0000_0050_00L
      case ReorderedGregorianTimeBased =>  expected(msb) == 0x00000000_0000_0060_00L
      case UnixEpochTimeBased          =>  expected(msb) == 0x00000000_0000_0070_00L
      case CustomFormatBased           =>  expected(msb) == 0x00000000_0000_0080_00L
      case Version9                    =>  expected(msb) == 0x00000000_0000_0090_00L
      case Version10                   =>  expected(msb) == 0x00000000_0000_00A0_00L
      case Version11                   =>  expected(msb) == 0x00000000_0000_00B0_00L
      case Version12                   =>  expected(msb) == 0x00000000_0000_00C0_00L
      case Version13                   =>  expected(msb) == 0x00000000_0000_00D0_00L
      case Version14                   =>  expected(msb) == 0x00000000_0000_00E0_00L
      case Version15                   =>  expected(msb) == 0x00000000_0000_00F0_00L
  }

  private def maskedValue(m: Long)(v: Long): Long =
    v & m

