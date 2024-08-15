package organisation

import cats.*

import io.circe.*
import io.circe.generic.semiauto.*

import na.*

case class Organisation(id: Option[Identity], name: String)

object Organisation:

  given organisationEncoder: Encoder[Organisation] =
    deriveEncoder[Organisation]

  given organisationDecoder: Decoder[Organisation] =
    deriveDecoder[Organisation]

  given organisationEntity[F[_]](using F: Monad[F]): HasIdentity[F, Organisation] =
    new HasIdentity[F, Organisation]:

      def id(organisation: Organisation): F[Option[Identity]] =
        F.pure(organisation.id)

      def withId(organisation: Organisation)(id: Identity): F[Organisation] =
        F.pure(organisation.copy(id = Some(id)))
