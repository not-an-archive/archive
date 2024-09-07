package na
package core

import org.scalacheck.*

object HealthProps extends Properties("na.core.Health"):

  import Prop.*
  import Health.*


  property("combine") =

    import cats.implicits.*
    import generators.given

    forAll: (l: Health, r: Health) =>
      (l, r) match
        case (Red, _)    => (l |+| r) == Red
        case (_, Red)    => (l |+| r) == Red
        case (Yellow, _) => (l |+| r) == Yellow
        case (_, Yellow) => (l |+| r) == Yellow
        case (_, _)      => (l |+| r) == l && (l |+| r) == r

  object generators:

    given Arbitrary[Health] =
      Arbitrary(Gen.oneOf(Health.values.toSeq))
