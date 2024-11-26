package na
package tools

import org.scalacheck.*

object DayProp extends Properties("na.tools.Day") :

  val genDay: Gen[Day] =
    for {
      year  <- Gen.choose(2022, 2030)
      month <- Gen.choose(1, 12)
      day   <- Gen.choose(1, 28)
    } yield Day(year, month, day)
