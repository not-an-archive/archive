package naa

import cats.*
import core.*

trait HasPID[F[_], A]:

  import PID.*
  import Born.*
  import Copy.*

  def pid(a: A): F[Option[PID]]

  def withPID(a: A)(id: PID): F[A]

  def withGeneratedPID(a: A): F[A] =
    withPID(a)(PID.random(born = Digitally, copy = External))


extension [F[_], A](fa: F[A])(using  F: FlatMap[F], E : HasPID[F, A])

  def pid: F[Option[PID]] =
    F.flatMap(fa)(E.pid)

  def withPID(id: PID): F[A] =
    F.flatMap(fa)(E.withPID(_)(id))

  def withPID: F[A] =
    F.flatMap(fa)(E.withGeneratedPID)