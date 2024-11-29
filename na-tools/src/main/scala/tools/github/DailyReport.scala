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

  val githubAPIs: Resource[IO, GithubAPIs[IO]] =
    EmberClientBuilder.default[IO].build.map(client => GithubClient(client, token))

  def run(args: List[String]): IO[ExitCode] =
    githubAPIs.use: github =>
      for {
        history <- RepoFile("na-nl", "archive", "authors.json")(github).history
        _ = println(s"history=\n${history.mkString("\n")}")
      } yield ExitCode.Success
