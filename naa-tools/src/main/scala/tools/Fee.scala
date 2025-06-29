package naa
package tools

import scala.util.matching.*

sealed trait Fee:
  override def toString: String =
    Fee.toString(this)

object Fee:

  val HourlyFee: Regex      = """(\d{2,3})/hour""".r
  val MonthlyFee: Regex     = """(\d{2,5})/month""".r
  val WeaponizedFee: String = """weaponized"""

  def toString(fee: Fee): String =
    fee match
      case Hourly(rate)  => s"$rate/hour"
      case Monthly(rate) => s"$rate/month"
      case Weaponized    => "weaponized"

  def fromString(str: String): Either[String,Fee] =
    str match
      case HourlyFee(rate)  => Hourly.fromRate(rate.toInt)
      case MonthlyFee(rate) => Monthly.fromRate(rate.toInt)
      case WeaponizedFee    => Right(Weaponized)
      case _                => Left(s"invalid fee: $str")

  import io.circe.*

  given Encoder[Fee] =
    Encoder.encodeString.contramap(_.toString)

  given Decoder[Fee] =
    Decoder.decodeString.emap(fromString)


case class Hourly(rate: Int) extends Fee

object Hourly:

  val MinRate = 70
  val MaxRate = 105

  def fromRate(rate: Int): Either[String,Fee] =
    if      (rate < MinRate) Left(s"min hourly rate $MinRate")
    else if (rate > MaxRate) Left(s"max hourly rate $MaxRate")
    else                     Right(Hourly(rate))


case class Monthly(rate: Int) extends Fee

object Monthly:

  val MinRate =  2800
  val MaxRate = 20000

  def fromRate(rate: Int): Either[String,Fee] =
    if      (rate < MinRate) Left(s"min monthly rate $MinRate")
    else if (rate > MaxRate) Left(s"max monthly rate $MaxRate")
    else                     Right(Monthly(rate))

case object Weaponized extends Fee
