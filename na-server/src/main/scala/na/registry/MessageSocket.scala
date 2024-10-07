package na.registry

import cats.implicits.*

import cats.effect.*
import cats.effect.std.*

import fs2.*
import fs2.io.net.*
import fs2.interop.scodec.*

import scodec.*

/**
 * Socket which reads a stream of messages of given type `A` and allows for parallel writing
 * of a given `bound`ed amount of messages of given type `B` after which the socket will
 * block on the `bound + 1`-th message before it will read a new `A`.
 */
trait MessageSocket[F[_],A,B]:
  def read: Stream[F,A]
  def write1(out: B): F[Unit]

object MessageSocket:

  def apply[F[_]:Concurrent,A,B](socket: Socket[F], decoder: Decoder[A], encoder: Encoder[B], bound: Int): F[MessageSocket[F,A,B]] =
    for outgoing <- Queue.bounded[F,B](bound)
      yield new MessageSocket[F,A,B]:
        def read: Stream[F,A] =
          val readSocket =
            socket
              .reads
              .through(StreamDecoder.many(decoder).toPipeByte[F])

          val writeOutput =
            Stream
              .fromQueueUnterminated(outgoing)
              .through(StreamEncoder.many(encoder).toPipeByte)
              .through(socket.writes)

          readSocket.concurrently(writeOutput)

        def write1(out: B): F[Unit] =
          outgoing.offer(out)
