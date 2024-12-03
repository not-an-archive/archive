package na
package tools

import na.tools.Day.fromLocalDate

import java.time.*
import scala.annotation.targetName

case class Day(year: Int, month: Int, day: Int):
  @targetName("lt")   def <(that: Day): Boolean  = implicitly[Ordering[Day]].lt(this, that)
  @targetName("gt")   def >(that: Day): Boolean  = implicitly[Ordering[Day]].gt(this, that)
  @targetName("eq")   def ==(that: Day): Boolean = implicitly[Ordering[Day]].eq(this, that)
  @targetName("ne")   def !=(that: Day): Boolean = implicitly[Ordering[Day]].ne(this, that)
  @targetName("lteq") def <=(that: Day): Boolean = implicitly[Ordering[Day]].lteq(this, that)
  @targetName("gteq") def >=(that: Day): Boolean = implicitly[Ordering[Day]].gteq(this, that)


  override def toString: String =
    f"$year%04d-$month%02d-$day%02d"

  def toShortDay: String =
    f"$day%02d"

  def asLocalDate: LocalDate =
    LocalDate.of(year, month, day)

  def next: Day =
    fromLocalDate(asLocalDate.plusDays(1))

object Day:

  import cats.effect.*

  def fromString(string: String): Day =
    string match
      case s"${year}-${month}-${day}" => Day(year.toInt, month.toInt, day.toInt)
      case _                          => sys.error(s"invalid day: $string")

  def fromLocalDate(date: LocalDate): Day =
    Day(date.getYear, date.getMonthValue, date.getDayOfMonth)

  def fromLocalDateTime(date: LocalDateTime): Day =
    Day(date.getYear, date.getMonthValue, date.getDayOfMonth)

  def today: IO[Day] =
    IO(fromLocalDate(LocalDate.now))

  def range(from: Day, to: Day): List[Day] =
    assert(from <= to, s"invalid range: from=$from, to=$to")
    def loop(acc: Vector[Day]): List[Day] =
      if acc.last > to then acc.toList else loop(acc :+ acc.last.next)
    loop(Vector(from))

  given Ordering[Day] =
    Ordering.by(_.asLocalDate)

