package na
package tools

case class Week(mon: Int, tue: Int, wed: Int, thu: Int, fri: Int, sat: Int, sun: Int):

  import java.time.*
  import DayOfWeek.*

  def getHoursByDate(day: Day): Int =
    day.asLocalDate.getDayOfWeek match
      case MONDAY    => mon
      case TUESDAY   => tue
      case WEDNESDAY => wed
      case THURSDAY  => thu
      case FRIDAY    => fri
      case SATURDAY  => sat
      case SUNDAY    => sun

object Week:

  def validated(mon: Int, tue: Int, wed: Int, thu: Int, fri: Int, sat: Int, sun: Int): Either[String ,Week] =
    if List(mon, tue, wed, thu, fri, sat, sun).forall(v => v >= 0 && v <= 24) then
      Right(Week(mon, tue, wed, thu, fri, sat, sun))
    else
      Left(s"hours not in range [0,24]")

  import io.circe.*
  import io.circe.generic.semiauto.*

  given Encoder[Week] =
    deriveEncoder[Week]

  given Decoder[Week] =
    deriveDecoder[Week]
