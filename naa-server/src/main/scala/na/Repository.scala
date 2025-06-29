package naa

import core.*

sealed trait StreamingRepository[F[_], A]:
  import fs2.Stream
  def stream: Stream[F, A]


abstract class CrudRepository[F[_], A]:
  type Result[A] = Either[RepositoryError, A]
  def create(a: A): F[Result[Unit]]
  def read(id: PID): F[Result[A]]
  def update(a: A): F[Result[Unit]]
  def delete(id: PID): F[Result[Unit]]


abstract class Repository[F[_], A](val name: String)
  extends CrudRepository[F, A]
  with StreamingRepository[F, A]

sealed trait RepositoryError
case class NotFoundError(name: String, id: PID) extends RepositoryError
case class NoPIDError(name: String)             extends RepositoryError
case class UpdateError(name: String, id: PID)   extends RepositoryError
