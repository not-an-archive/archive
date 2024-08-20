package na

import cats.*
import core.*

trait HasPID[F[_], A]:

  def id(a: A): F[Option[PID]]

  def withId(a: A)(id: PID): F[A]

  def withGeneratedId(a: A): F[A] =
    withId(a)(PID.random(digitallyBorn = true))


extension [F[_], A](fa: F[A])(using  F: FlatMap[F], E : HasPID[F, A])

  def id: F[Option[PID]] =
    F.flatMap(fa)(E.id)

  def withId(id: PID): F[A] =
    F.flatMap(fa)(E.withId(_)(id))

  def withGeneratedId: F[A] =
    F.flatMap(fa)(E.withGeneratedId)