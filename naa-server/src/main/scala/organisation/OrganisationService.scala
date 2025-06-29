package organisation

import cats.effect.*

import naa.*

object OrganisationService:

  def apply(repository: Repository[IO, Organisation]): Service[Organisation] =
    new Service[Organisation]("organisations", repository)
