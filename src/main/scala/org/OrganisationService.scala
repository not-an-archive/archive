package org

import cats.effect.*

import na.nl.*

object OrganisationService:

  def apply(repository: Repository[IO, Organisation]): Service[Organisation] =
    new Service[Organisation]("organisations", repository)
