package na
package tools

case class Author(github: String, team: String, name: String, status: AuthorStatus, credit: Week, debit: Week, fee: Fee):

  def creditHoursBy(day: Day)(authorStatus: AuthorStatus): Int =
    if status == authorStatus then credit.getHoursFor(day) else 0

  def debitHoursBy(day: Day)(authorStatus: AuthorStatus): Int =
    if status == authorStatus then debit.getHoursFor(day) else 0


object Author:

  import io.circe.*
  import io.circe.generic.semiauto.*

  given Encoder[Author] =
    deriveEncoder[Author]

  given Decoder[Author] =
    deriveDecoder[Author]
