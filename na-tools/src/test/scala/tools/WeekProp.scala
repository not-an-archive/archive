package na
package tools

import org.scalacheck.*

import java.time.DayOfWeek

object WeekProp extends Properties("na.tools.Week") :

  import DayProp.*
  import Prop.*

  import DayOfWeek.*

  val genHoursPerDay: Gen[Int] =
    Gen.choose(0, 24)

  val genWeek: Gen[Week] = for {
    mon <- genHoursPerDay
    tue <- genHoursPerDay
    wed <- genHoursPerDay
    thu <- genHoursPerDay
    fri <- genHoursPerDay
    sat <- genHoursPerDay
    sun <- genHoursPerDay
  } yield Week(mon, tue, wed, thu, fri, sat, sun)


  val fixture: Week = Week(1, 2, 3, 4, 5, 6, 7)

  property("getHoursForDay") =
    forAll(genDay) { (day: Day) =>
      fixture.getHoursFor(day) ==
        (day.asLocalDate.getDayOfWeek match
          case MONDAY    => fixture.mon
          case TUESDAY   => fixture.tue
          case WEDNESDAY => fixture.wed
          case THURSDAY  => fixture.thu
          case FRIDAY    => fixture.fri
          case SATURDAY  => fixture.sat
          case SUNDAY    => fixture.sun
          )
    }

  import io.circe.syntax.*

  property("codec round-trip") =
    forAll(genWeek) { (actual: Week) =>
      actual.asJson.as[Week].contains(actual)
    }
