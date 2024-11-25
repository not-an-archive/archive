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
      parse(Source.fromFile("authors.json").mkString)
        .map(x => { val y = x.as[Authors] ; println(y) ; y.getOrElse(sys.error("invalid json format: authors.json")) } )
        .getOrElse(sys.error("invalid format: authors.json"))
    }

  given Encoder[Authors] =
    deriveEncoder[Authors]

  given Decoder[Authors] =
    deriveDecoder[Authors]