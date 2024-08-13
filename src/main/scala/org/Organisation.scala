package org

import cats.*

import io.circe.*
import io.circe.generic.semiauto.*

import na.nl.*

case class Organisation(id: Option[Identity], name: String)

object Organisation:

  implicit val organisationEncoder: Encoder[Organisation] =
    deriveEncoder[Organisation]

  implicit val organisationDecoder: Decoder[Organisation] =
    deriveDecoder[Organisation]

  implicit def organisationEntity[F[_]](implicit F: Monad[F]): HasIdentity[F, Organisation] =
    new HasIdentity[F, Organisation]:

      def id(organisation: Organisation): F[Option[Identity]] =
        F.pure(organisation.id)

      def withId(organisation: Organisation)(id: Identity): F[Organisation] =
        F.pure(organisation.copy(id = Some(id)))
