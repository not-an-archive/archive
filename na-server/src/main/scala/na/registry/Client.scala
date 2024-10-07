package na.registry

import cats.ApplicativeError
import cats.effect.{Concurrent, Temporal}
import com.comcast.ip4s.{IpAddress, SocketAddress}
import fs2.io.net.Network
import fs2.{RaiseThrowable, Stream}

import java.net.ConnectException
import scala.concurrent.duration.*

object Client:

  import Protocol.*

  private val connectRetry = 1.second

  def start[F[_]:Temporal:Network:Console](address: SocketAddress[IpAddress]): Stream[F,Unit] =
    connect(address)
      .handleErrorWith:
        case _ : ConnectException =>
          val log     = Stream.exec(Console[F].errorln(s"Failed to connect. Retrying in $connectRetry."))
          val request = start(address).delayBy(connectRetry)
          log ++ request
        case _ : UserQuit =>
          Stream.empty
        case t =>
          Stream.raiseError(t)

  private def connect[F[_]:Temporal:Concurrent:Network:Console](address: SocketAddress[IpAddress]): Stream[F,Unit] =
    val log =
      Stream.exec(Console[F].info(s"Connecting to server $address"))

    val request =
      Stream
        .resource(Network[F].client(address))
        .flatMap: socket =>
          val log =
            Stream.exec(Console[F].info("🎉 Connected! 🎊"))

          val process =
            Stream
              .eval(MessageSocket(socket, ServerCommand.codec, ClientCommand.codec, 128))
              .flatMap(messageSocket => inbound(messageSocket).concurrently(outbound(messageSocket)))

          log ++ process
    log ++ request

  private def inbound[F[_]:Console](messageSocket: MessageSocket[F,ServerCommand,ClientCommand])(implicit F: ApplicativeError[F, Throwable]): Stream[F, Unit] =
    messageSocket
      .read
      .evalMap:
        case Protocol.ServerCommand.Tock =>
          Console[F].println(s"inbound client: tock [${System.nanoTime}]")

  private def outbound[F[_]:Temporal:RaiseThrowable:Console](messageSocket: MessageSocket[F,ServerCommand,ClientCommand]): Stream[F, Unit] =
    Stream
      .fixedRate(1.second)
      .evalMap(_ => Console[F].println(s"outbound client: tick [${System.nanoTime}]"))
      .map(_ => ClientCommand.Tick(System.nanoTime))
      .evalMap(messageSocket.write1)
