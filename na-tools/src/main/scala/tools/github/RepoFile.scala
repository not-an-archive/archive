package na
package tools
package github

import cats.effect.*
import cats.syntax.all.*
import github4s.algebras.GithubAPIs
import github4s.domain.Commit

case class RepoFile(organisation: String, repository: String, path: String, github: GithubAPIs[IO]):

  def getMainCommits: IO[List[Commit]] =
    github
      .repos
      .listCommits(organisation, repository, None, Some(path))
      .flatMap(response => IO.fromEither(response.result))

  private def decodeBase64(base64String: String): String =
    import java.nio.charset.StandardCharsets.UTF_8
    import java.util.Base64
    String(Base64.getMimeDecoder.decode(base64String.trim), UTF_8)

  def history: IO[List[(Day, String)]] =
    getMainCommits.flatMap(_.map(commit => loadFileContent(commit.sha).map(content => (Day.fromString(commit.date), content))).parSequence)

  private def loadFileContent(commit: String): IO[String] =
    github
      .repos
      .getContents(organisation, repository, path, Some(commit))
      .flatMap(response => IO.fromEither(response.result))
      .map(_.head)
      .map(x => { val y = decodeBase64(x.content.getOrElse("<empty>")) ; println(s"loaded\n$y") ; y })

  def getFileSHA: IO[String] =
    github
      .repos
      .getContents(organisation, repository, path)
      .flatMap(response => IO.fromEither(response.result))
      .map(x => x.head.sha)

  def mkString: IO[String] =

    def decodeBase64(base64String: String): IO[String] =
      import java.nio.charset.StandardCharsets.UTF_8
      import java.util.Base64
      IO(String(Base64.getMimeDecoder.decode(base64String.trim), UTF_8))

    for {
      sha <- getFileSHA
      _ <- getMainCommits
      str <- github
        .gitData
        .getBlob(organisation, repository, sha)
        .flatMap(response => IO.fromEither(response.result))
        .flatMap(contents => IO.fromOption(contents.content)(sys.error(s"no contents: $organisation/$repository/$path")))
        .flatMap(decodeBase64)
    } yield str