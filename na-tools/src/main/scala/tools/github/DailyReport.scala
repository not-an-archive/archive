package na
package tools
package github

import cats.effect.*
import org.http4s.ember.client.*
import github4s.*
import github4s.algebras.GithubAPIs


object DailyReport extends IOApp:

  import io.circe.parser.*

  val token: Option[String] =
    None

  val githubAPIs: Resource[IO, GithubAPIs[IO]] =
    EmberClientBuilder.default[IO].build.map(client => GithubClient(client, token))

  def validOn(day: Day, history: Map[Day,Authors]): Option[Authors] =
    history
      .filter((d,_) => d <= day)
      .toList
      .sortBy(_._1)
      .map(_._2)
      .lastOption

  def creditOn(day: Day, github: String, status: AuthorStatus, authors: Authors): Int =
    authors.authors
      .filter(_.github == github)
      .map(_.creditHoursBy(day)(status))
      .headOption // could validate that we only have one author
      .getOrElse(0)

  def run(args: List[String]): IO[ExitCode] =
    githubAPIs.use: github =>
      RepoFile("na-nl", "archive", "authors.json")(github)
        .history
        .map: contents =>
          val history: Map[Day, Authors] =
            contents
              .map((date,cs)    => Day.fromLocalDateTime(date) -> decode[Authors](cs))
              .filter((_,r) => r.isRight)
              .map((d,as)   => d -> as.getOrElse(sys.error(s"invalid state: $as")))

          Day
            .range(Day.fromString("2024-01-01"), Day.fromString("2024-12-31"))
            .map: day =>
              validOn(day, history) match
                case None    =>
                  println(s"day=$day, credit=<unknown>, debit=<unknown>, fee=<unknown>")
                case Some(authors) =>
                  val credit  = creditOn(day, "nmcb", AuthorStatus.Active, authors)
                  println(s"day=$day, credit=$credit, debit=<unknown>, fee=<unknown>")

        .as(ExitCode.Success)
