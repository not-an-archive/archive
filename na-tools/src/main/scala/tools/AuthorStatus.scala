package na
package tools

enum AuthorStatus(override val toString: String):
  case Active   extends AuthorStatus("active")
  case Sick     extends AuthorStatus("sick")
  case Vacation extends AuthorStatus("vacation")
  case Inactive extends AuthorStatus("inactive")

  def isActive:   Boolean = this == Active
  def isSick:     Boolean = this == Sick
  def isVacation: Boolean = this == Vacation
  def isInactive: Boolean = this == Inactive

  def requiresDailyLogLine: Boolean =
    !this.isInactive


object AuthorStatus:

  def fromString(s: String): Either[String,AuthorStatus] =
    values.find(_.toString == s) match
      case Some(status) => Right(status)
      case None         => Left(s"invalid author status: $s")

  import io.circe.*
  
  given Encoder[AuthorStatus] =
    Encoder.encodeString.contramap(_.toString)
  
  given Decoder[AuthorStatus] =
    Decoder.decodeString.emap(fromString)
