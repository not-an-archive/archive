package na
package tools

import java.time.*

case class Day(year: Int, month: Int, day: Int):

  override def toString: String =
    f"$year%04d-$month%02d-$day%02d"

  def toShortDay: String =
    f"$day%02d"

  def asLocalDate: LocalDate =
    LocalDate.of(year, month, day)

object Day:

  import cats.effect.*

  def fromLocalDate(date: LocalDate): Day =
    Day(date.getYear, date.getMonthValue, date.getDayOfMonth)

  def today: IO[Day] =
    IO(fromLocalDate(LocalDate.now))
