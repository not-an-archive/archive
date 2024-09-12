package na.core

import org.scalacheck.*

object PIDProps_given extends Properties("na.core.PID.given"):

  import org.scalactic.*
  import TypeCheckedTripleEquals.*

  import Prop.*
  import generators.given

  property("pidEncoder/Decoder round trip") =
    import io.circe.*
    forAll: (pid: PID) =>
      val encoder = summon[Encoder[PID]]
      val decoder = summon[Decoder[PID]]
      val encoded = encoder(pid)
      val decoded = decoder.decodeJson(encoded).getOrElse(sys.error("decode error"))
      decoded === pid

  property("segmentEncoder") =
    import org.http4s.Uri.Path.SegmentEncoder
    forAll: (pid: PID) =>
      val encoder = summon[SegmentEncoder[PID]]
      encoder.toSegment(pid).toString === pid.toString


  object generators:

    import Arbitrary.*

    val applyPIDs: Gen[PID] =
      for {
        msb <- arbitrary[Long]
        lsb <- arbitrary[Long]
      } yield PID(msb, lsb)

    given arbitraryApplyPID: Arbitrary[PID] =
      Arbitrary(applyPIDs)
