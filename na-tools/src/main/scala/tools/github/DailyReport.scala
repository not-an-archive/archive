package na
package tools
package github

import cats.effect.*
import org.http4s.ember.client.*
import github4s.*
import github4s.algebras.GithubAPIs


object DailyReport extends IOApp:

  val token: Option[String] =
    None

  val github: Resource[IO, GithubAPIs[IO]] =
    EmberClientBuilder.default[IO].build.map(client => GithubClient(client, token))

  def run(args: List[String]): IO[ExitCode] =
    github.use: apis =>
      for {
        authors <- RepoFile("na-nl", "archive", "authors.json", apis).mkString
        _ = println(s"authors=\n$authors")
        history <- RepoFile("na-nl", "archive", "authors.json", apis).history
        _ = println(s"history=\n${history.mkString("\n")}")
      } yield ExitCode.Success
