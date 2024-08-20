package na

import core.*

import cats.effect.*
import cats.data.*
import cats.implicits.*

import fs2.Stream

import io.circe.*
import io.circe.syntax.*

import org.http4s.*
import org.http4s.circe.*
import org.http4s.headers.*

import scala.util.*

class Service[A : Decoder : Encoder](segment: String, repository: Repository[IO, A])(using E : HasPID[IO, A])
  extends dsl.Http4sDsl[IO]:

  type EndPoint = Kleisli[IO, Request[IO], Response[IO]]
  val  EndPoint = Kleisli

  def http = HttpRoutes.of[IO] {
    case req @ POST    -> Root / `segment`               =>  create(req)
    case req @ PUT     -> Root / `segment` / PIDVar(id)  =>  update(id)(req)
    case req @ GET     -> Root / `segment` / PIDVar(id)  =>  read(id)(req)
    case req @ DELETE  -> Root / `segment` / PIDVar(id)  =>  delete(id)(req)
    case req @ GET     -> Root / `segment`               =>  stream(req)
  }

  def create: EndPoint =
    EndPoint(
      req => for {
        entity   <- req.decodeJson[A].withGeneratedPID
        result   <- repository.create(entity)
        response <- httpCreatedOr500(entity)(result)
      } yield response
    )

  def update(id: PID): EndPoint =
    EndPoint(
      req => for {
        entity   <- req.decodeJson[A].withPID(id)
        result   <- repository.update(entity)
        response <- httpOkOr404(entity)(result)
      } yield response
    )

  def read(id: PID): EndPoint =
    EndPoint(_ => repository.read(id).flatMap(httpOkOr404))

  def delete(id: PID): EndPoint =
    EndPoint(
      _  => repository.delete(id).flatMap {
        case Left(NotFoundError(_, _)) => NotFound()
        case Left(error)               => InternalServerError(error.toString)
        case Right(_)                  => NoContent()
      }
    )

  def stream: EndPoint =
    EndPoint(
      _ => Ok(
        Stream("[")
        ++ repository.stream.map(_.asJson.noSpaces).intersperse(",")
        ++ Stream("]"),
        `Content-Type`(new MediaType("application", "json"))
      )
    )

  private def httpOkOr404(a: A)(result: Either[?,?]): IO[Response[IO]] =
    result match {
      case Left(_)  => NotFound()
      case _        => Ok(a.asJson)
    }

  private def httpOkOr404(result: Either[?,A]): IO[Response[IO]] =
    result match {
      case Left(_)  => NotFound()
      case Right(a) => Ok(a.asJson)
    }

  private def httpCreatedOr500(a: A)(result: Either[?,?]): IO[Response[IO]] =
    result match {
      case Left(e)  =>
        InternalServerError(e.toString)
      case _        =>
        E.pid(a).flatMap(id => Created(a.asJson, Location(Uri.unsafeFromString(s"/$segment/${id.get}"))))
    }


object PIDVar extends PathVar(PID.fromString)

protected class PathVar[A](cast: String => A):
  def unapply(str: String): Option[A] =
    Try(cast(str)).toOption
