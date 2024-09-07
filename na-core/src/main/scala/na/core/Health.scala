package na
package core

enum Health:
  case Green
  case Yellow
  case Red

object Health:

  import cats.Monoid

  /** safety first i.e. escalating combining; note that this requires empty health to be green */
  given Monoid[Health] =
    Monoid.instance(
      emptyValue = Green,
      (_, _) match
        case (Red, Red)       => Red
        case (Red, Yellow)    => Red
        case (Red, Green)     => Red
        case (Yellow, Red)    => Red
        case (Yellow, Yellow) => Yellow
        case (Yellow, Green)  => Yellow
        case (Green, Red)     => Red
        case (Green, Yellow)  => Yellow
        case (Green, Green)   => Green
    )
