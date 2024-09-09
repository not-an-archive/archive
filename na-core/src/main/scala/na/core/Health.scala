package na
package core

enum Health:
  case Red
  case Yellow
  case Green

object Health:

  import cats.Monoid

  /** Safety first, i.e. combines escalating towards Red. */
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
