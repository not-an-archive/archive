package organisation

import cats.effect.*

import na.*

object OrganisationService:

  def apply(repository: Repository[IO, Organisation]): Service[Organisation] =
    new Service[Organisation]("organisations", repository)
