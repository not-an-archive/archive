package organisation

import cats.*
import naa.core.*

import io.circe.*
import io.circe.generic.semiauto.*

import naa.*

case class Organisation(pid: Option[PID], name: String)

object Organisation:

  given organisationEncoder: Encoder[Organisation] =
    deriveEncoder[Organisation]

  given organisationDecoder: Decoder[Organisation] =
    deriveDecoder[Organisation]

  given organisationEntity[F[_]](using F: Monad[F]): HasPID[F, Organisation] =
    new HasPID[F, Organisation]:

      def pid(organisation: Organisation): F[Option[PID]] =
        F.pure(organisation.pid)

      def withPID(organisation: Organisation)(pid: PID): F[Organisation] =
        F.pure(organisation.copy(pid = Some(pid)))
