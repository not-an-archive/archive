package na
package tools

import org.scalacheck.*

object AuthorProp extends Properties("tools.Author"):

  import AuthorStatusProp.*
  import DayProp.*
  import WeekProp.*
  import FeeProp.*

  import Gen.*
  import Prop.*

  val genGithub: Gen[String] =
    for {
      length <- choose(4, 20)
      chars  <- stringOfN(length, alphaLowerChar)
    } yield chars

  val genTeam: Gen[String] =
    for {
      length <- choose(4, 20)
      chars  <- stringOfN(length, alphaLowerChar)
    } yield s"#$chars"

  val genName: Gen[String] =
    for {
      length <- choose(4, 20)
      chars  <- stringOfN(length, alphaLowerChar)
    } yield chars

  val genAuthor: Gen[Author] =
    for {
      github <- genGithub
      team   <- genTeam
      name   <- genName
      status <- genAuthorStatus
      credit <- genWeek
      debit  <- genWeek
      fee    <- genFee
    } yield Author(
      github = github,
      team   = team,
      name   = name,
      status = status,
      credit = credit,
      debit  = debit,
      fee    = fee
    )

  def calculatesHoursFrom(expected: Author => Week, actual: Author => Day => AuthorStatus => Int) =
    forAll(genDay, genAuthor, genAuthorStatus) { (day: Day, author: Author, status: AuthorStatus) =>
      if author.status == status then
        expected(author).getHoursByDate(day) == actual(author)(day)(status)
      else
        actual(author)(day)(status) == 0
    }

  property("debitHoursBy") =
    calculatesHoursFrom(_.debit, _.debitHoursBy)

  property("creditHoursBy") =
    calculatesHoursFrom(_.credit, _.creditHoursBy)
    
  import io.circe.syntax.*

  property("codec roundtrip") =
    forAll(genAuthor) { (actual: Author) =>
      actual.asJson.as[Author].contains(actual)
    }
