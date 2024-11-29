package na
package tools

import org.scalacheck.*

object DayProp extends Properties("na.tools.Day"):

  import java.time.*

  import Prop.*

  val genDay: Gen[Day] =
    for {
      year  <- Gen.choose(2022, 2030)
      month <- Gen.choose(1, 12)
      day   <- Gen.choose(1, 28)
    } yield Day(year, month, day)

  val genLocalDate: Gen[LocalDate] =
    Gen.choose[LocalDate](LocalDate.MIN, LocalDate.MAX)

  val genLocalDateTime: Gen[LocalDateTime] =
    Gen.choose[LocalDateTime](LocalDateTime.MIN, LocalDateTime.MAX)

  property("fromLocalDate") =
    forAll(genLocalDate) { (date: LocalDate) =>
      val day = Day.fromLocalDate(date)
      date.getYear == day.year && date.getMonthValue == day.month && date.getDayOfMonth == day.day
    }

  property("fromLocalDateTime") =
    forAll(genLocalDateTime) { (date: LocalDateTime) =>
      val day = Day.fromLocalDateTime(date)
      date.getYear == day.year && date.getMonthValue == day.month && date.getDayOfMonth == day.day
    }
