package naa
package tools

import org.scalacheck.*

object AuthorStatusProp extends Properties("naa.tools.AuthorStatus"):

  import AuthorStatus.*
  import Prop.*

  val genAuthorStatus: Gen[AuthorStatus] =
    Gen.oneOf(AuthorStatus.values.toIndexedSeq)

  property("fromString") =
    forAll(genAuthorStatus) { (status: AuthorStatus) =>
      fromString(status.toString).exists(_.toString == status.toString)
    }

  import io.circe.syntax.*

  property("codec round-trip") =
    forAll(genAuthorStatus) { (actual: AuthorStatus) =>
      actual.asJson.as[AuthorStatus].contains(actual)
    }
