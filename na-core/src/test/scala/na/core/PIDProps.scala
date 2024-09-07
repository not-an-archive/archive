package na
package core

import org.scalacheck.*

import PID.*

object PIDProps extends Properties("na.core.PID"):

  import util.*
  import Prop.*

  property("variant") =
    forAll: (msb: Long, lsb: Long) =>
      import Variant.*
      PID(msb, lsb).variant match
        case Reserved                     => lsb.toBinaryString.startsWith("111")
        case MicrosoftBackwardsCompatible => lsb.toBinaryString.startsWith("110")
        case LeachSalz                    => lsb.toBinaryString.startsWith("10")
        case NCSBackwardsCompatible       => true

  property("version") =
    forAll: (msb: Long, lsb: Long) =>
      import Version.*
      val expected = maskedValue(0x00000000_0000_F000L)
      PID(msb, lsb).version match
        case Unused                      =>  expected(msb) == 0x00000000_0000_0000L
        case GregorianTimeBased          =>  expected(msb) == 0x00000000_0000_1000L
        case DCESecurityBased            =>  expected(msb) == 0x00000000_0000_2000L
        case MD5HashNameBased            =>  expected(msb) == 0x00000000_0000_3000L
        case RandomGeneratedBased        =>  expected(msb) == 0x00000000_0000_4000L
        case SHA1HashNameBased           =>  expected(msb) == 0x00000000_0000_5000L
        case ReorderedGregorianTimeBased =>  expected(msb) == 0x00000000_0000_6000L
        case UnixEpochTimeBased          =>  expected(msb) == 0x00000000_0000_7000L
        case CustomFormatBased           =>  expected(msb) == 0x00000000_0000_8000L
        case Version9                    =>  expected(msb) == 0x00000000_0000_9000L
        case Version10                   =>  expected(msb) == 0x00000000_0000_A000L
        case Version11                   =>  expected(msb) == 0x00000000_0000_B000L
        case Version12                   =>  expected(msb) == 0x00000000_0000_C000L
        case Version13                   =>  expected(msb) == 0x00000000_0000_D000L
        case Version14                   =>  expected(msb) == 0x00000000_0000_E000L
        case Version15                   =>  expected(msb) == 0x00000000_0000_F000L

  property("born") =
    import Born.*
    forAll: (msb: Long, lsb: Long) =>
      val expected = maskedValue(0x0000_000000000001L)
      PID(msb, lsb).born match
        case Digitally  => expected(lsb) == 0x0000_000000000000L
        case Physically => expected(lsb) == 0x0000_000000000001L

  property("copy") =
    import Copy.*
    forAll: (msb: Long, lsb: Long) =>
      val expected = maskedValue(0x0000_000000000006L)
      PID(msb, lsb).copy match
        case External  => expected(lsb) == 0x0000_000000000000L
        case Internal1 => expected(lsb) == 0x0000_000000000002L
        case Internal2 => expected(lsb) == 0x0000_000000000004L
        case Internal3 => expected(lsb) == 0x0000_000000000006L

  property("Born.fromOrdinal") =
    import scala.util.*
    import Born.*
    forAll: (ordinal: Int) =>
      ordinal match
        case 0 => Try(Born.fromOrdinal(ordinal)).get == Digitally
        case 1 => Try(Born.fromOrdinal(ordinal)).get == Physically
        case _ => Try(Born.fromOrdinal(ordinal)).isFailure

  property("Copy.fromOrdinal") =
    import scala.util.*
    import Copy.*
    forAll: (ordinal: Int) =>
      ordinal match
        case 0 => Try(Copy.fromOrdinal(ordinal)).get == External
        case 1 => Try(Copy.fromOrdinal(ordinal)).get == Internal1
        case 2 => Try(Copy.fromOrdinal(ordinal)).get == Internal2
        case 3 => Try(Copy.fromOrdinal(ordinal)).get == Internal3
        case _ => Try(Copy.fromOrdinal(ordinal)).isFailure

  private def maskedValue(m: Long)(v: Long): Long =
    v & m
