package na
package tools

import cats.effect.*

case class Authors(authors: List[Author])

object Authors:

  import scala.io.*
  import io.circe.*
  import io.circe.parser.*
  import io.circe.generic.semiauto.*

  val localAuthors: IO[Authors] =
    IO {
      val file = Source.fromFile("authors.json")
      parse(file.mkString) match
        case Left(failure) => sys.error("invalid format: authors.json")
        case Right(json)   => json.as[Authors].getOrElse(sys.error("invalid json format: authors.json"))
    }

  given Encoder[Authors] =
    deriveEncoder[Authors]

  given Decoder[Authors] =
    deriveDecoder[Authors]
