package na
package core

import org.scalacheck.*
import org.scalactic.*

object PIDProps extends Properties("na.core.PID"):

  import util.*
  import Prop.*
  import TypeCheckedTripleEquals.*
  import PID.*

  import generators.given

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
        case Unused                      =>  expected(msb) === 0x00000000_0000_0000L
        case GregorianTimeBased          =>  expected(msb) === 0x00000000_0000_1000L
        case DCESecurityBased            =>  expected(msb) === 0x00000000_0000_2000L
        case MD5HashNameBased            =>  expected(msb) === 0x00000000_0000_3000L
        case RandomGeneratedBased        =>  expected(msb) === 0x00000000_0000_4000L
        case SHA1HashNameBased           =>  expected(msb) === 0x00000000_0000_5000L
        case ReorderedGregorianTimeBased =>  expected(msb) === 0x00000000_0000_6000L
        case UnixEpochTimeBased          =>  expected(msb) === 0x00000000_0000_7000L
        case CustomFormatBased           =>  expected(msb) === 0x00000000_0000_8000L
        case Version9                    =>  expected(msb) === 0x00000000_0000_9000L
        case Version10                   =>  expected(msb) === 0x00000000_0000_A000L
        case Version11                   =>  expected(msb) === 0x00000000_0000_B000L
        case Version12                   =>  expected(msb) === 0x00000000_0000_C000L
        case Version13                   =>  expected(msb) === 0x00000000_0000_D000L
        case Version14                   =>  expected(msb) === 0x00000000_0000_E000L
        case Version15                   =>  expected(msb) === 0x00000000_0000_F000L

  property("born") =
    import Born.*
    forAll: (msb: Long, lsb: Long) =>
      val expected = maskedValue(0x0000_000000000001L)
      PID(msb, lsb).born match
        case Digitally  => expected(lsb) === 0x0000_000000000000L
        case Physically => expected(lsb) === 0x0000_000000000001L

  property("copy") =
    import Copy.*
    forAll: (msb: Long, lsb: Long) =>
      val expected = maskedValue(0x0000_000000000006L)
      PID(msb, lsb).copy match
        case External  => expected(lsb) === 0x0000_000000000000L
        case Internal1 => expected(lsb) === 0x0000_000000000002L
        case Internal2 => expected(lsb) === 0x0000_000000000004L
        case Internal3 => expected(lsb) === 0x0000_000000000006L

  property("Born.fromOrdinal") =
    import scala.util.*
    import Born.*
    forAll: (ordinal: Int) =>
      ordinal match
        case 0 => Try(Born.fromOrdinal(ordinal)).get === Digitally
        case 1 => Try(Born.fromOrdinal(ordinal)).get === Physically
        case _ => Try(Born.fromOrdinal(ordinal)).isFailure

  property("Copy.fromOrdinal") =
    import scala.util.*
    import Copy.*
    forAll: (ordinal: Int) =>
      ordinal match
        case 0 => Try(Copy.fromOrdinal(ordinal)).get === External
        case 1 => Try(Copy.fromOrdinal(ordinal)).get === Internal1
        case 2 => Try(Copy.fromOrdinal(ordinal)).get === Internal2
        case 3 => Try(Copy.fromOrdinal(ordinal)).get === Internal3
        case _ => Try(Copy.fromOrdinal(ordinal)).isFailure

  property("embed") =
    import Copy.*
    import Born.*
    forAll: (pid: PID) =>
      pid.embed(External, significant = false).copy === External
      pid.embed(Internal1, significant = false).copy === Internal1
      pid.embed(Internal2, significant = false).copy === Internal2
      pid.embed(Internal3, significant = false).copy === Internal3
      pid.embed(Digitally, significant = false).born === Digitally
      pid.embed(Physically, significant = false).born === Physically

  private def maskedValue(m: Long)(v: Long): Long =
    v & m


  property("circe.Encoder.Decoder round trip") =
    import io.circe.*
    val encoder = summon[Encoder[PID]]
    val decoder = summon[Decoder[PID]]

    forAll: (pid: PID) =>
      val encoded = encoder(pid)
      val decoded = decoder.decodeJson(encoded).getOrElse(sys.error("decode error"))
      decoded === pid

  property("http4s.SegmentEncoder") =
    import org.http4s.Uri.Path.SegmentEncoder
    val encoder = summon[SegmentEncoder[PID]]

    forAll: (pid: PID) =>
      encoder.toSegment(pid).toString === pid.toString

  property("extension.toUUID.toPID") =
    forAll: (pid: PID) =>
      pid.toUUID.toPID === pid

  property("extension.asExternal") =
    import Copy.*
    forAll: (pid: PID) =>
      pid.asExternal.copy === External

  object generators:

    import Arbitrary.*

    val applyPIDs: Gen[PID] =
      for {
        msb <- arbitrary[Long]
        lsb <- arbitrary[Long]
      } yield PID(msb, lsb)

    given arbitraryApplyPID: Arbitrary[PID] =
      Arbitrary(applyPIDs)
