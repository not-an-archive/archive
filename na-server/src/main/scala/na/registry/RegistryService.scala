package na
package registry

import core.*

import cats.effect.*
import cats.data.*

import fs2.Stream

import io.circe.*
import io.circe.syntax.*

import org.http4s.*
import org.http4s.headers.*

import scala.util.*

class Service[A : Decoder : Encoder](registry: Registry) extends dsl.Http4sDsl[IO]:

  type EndPoint = Kleisli[IO, Request[IO], Response[IO]]
  val  EndPoint = Kleisli

  def http = HttpRoutes.of[IO] {
    case req @ POST -> Root / "register" / PIDVar(pid) => register(pid)(req)
    case req @ GET  -> Root / "register"               => stream(req)
  }

  def register(pid: PID): EndPoint =
    EndPoint(
      req => for {
        result   <- registry.register(pid)
        response <- InternalServerError("unimplemented")
      } yield response
    )

  def stream: EndPoint =
    EndPoint(
      _ => Ok(
        Stream("[")
        ++ registry.stream.map(_.asJson.noSpaces).intersperse(",")
        ++ Stream("]"),
        `Content-Type`(new MediaType("application", "json"))
      )
    )

object PIDVar extends PathVar(PID.fromString)

protected class PathVar[A](cast: String => A):
  def unapply(str: String): Option[A] =
    Try(cast(str)).toOption
