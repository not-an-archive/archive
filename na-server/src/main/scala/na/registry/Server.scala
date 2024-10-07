package na.registry

import cats.{FlatMap, MonadError}
import cats.effect.{Async, Concurrent, Ref, Sync}
import cats.effect.std.UUIDGen
import cats.implicits._
import com.comcast.ip4s.Port
import fs2.Stream
import fs2.io.net.{Network, Socket}
import java.util.UUID

object Server:

  import Protocol.*

  private case class ConnectedClient[F[_]](id: UUID, socket: MessageSocket[F,ClientCommand,ServerCommand])

  private object ConnectedClient:
    def apply[F[_] : Concurrent : UUIDGen](socket: Socket[F]): F[ConnectedClient[F]] =
      for
        id            <- UUIDGen[F].randomUUID
        messageSocket <- MessageSocket(socket, ClientCommand.codec, ServerCommand.codec, 1024)
      yield ConnectedClient(id, messageSocket)

  private class Clients[F[_]:Concurrent](ref: Ref[F,Map[UUID,ConnectedClient[F]]]):

    def get(id: UUID): F[Option[ConnectedClient[F]]] =
      ref.get.map(_.get(id))

    def all: F[List[ConnectedClient[F]]] =
      ref.get.map(_.values.toList)

    def register(state: ConnectedClient[F]): F[Unit] =
      ref.update(oldClients => oldClients + (state.id -> state))

    def unregister(id: UUID): F[Option[ConnectedClient[F]]] =
      ref.modify(old => (old - id, old.get(id)))

  private object Clients:
    def apply[F[_]: Concurrent]: F[Clients[F]] =
      Ref[F]
        .of(Map.empty[UUID,ConnectedClient[F]])
        .map(ref => new Clients(ref))

  def start[F[_]: Async: Network: Console](port: Port) =

    val log =
      Stream.exec(Console[F].info(s"Starting server on port $port"))

    val serve =
      Stream
        .eval(Clients[F])
        .flatMap: clients =>
          Network[F]
            .server(port = Some(port))
            .map: clientSocket =>
              def unregisterClient(client: ConnectedClient[F]) =
                clients.unregister(client.id) *> Console[F].info(s"Unregistered client ${client.id}")
              Stream
                .bracket(ConnectedClient[F](clientSocket).flatTap(clients.register))(unregisterClient)
                .flatMap(client => handleClient[F](clients, client, clientSocket))
                .scope
        .parJoinUnbounded

    log ++ serve

  private def handleClient[F[_] : Concurrent : Console](clients: Clients[F], client: ConnectedClient[F], socket: Socket[F]): Stream[F,Nothing] =
    val log     = logNewClient(client, socket)
    val process = processIncoming(clients, client.id, client.socket)
    (log ++ process)
      .handleErrorWith:
        case _: UserQuit =>
          Stream.exec(Console[F].info(s"Client quit ${client.id}"))
        case err =>
          Stream.exec(Console[F].errorln(s"Fatal error for client ${client.id} - $err"))

  private def logNewClient[F[_] : FlatMap : Console](client: ConnectedClient[F], socket: Socket[F]): Stream[F,Nothing] =
    Stream.exec(socket.remoteAddress.flatMap(address => Console[F].info(s"Accepted client ${client.id} on $address")))

  private def processIncoming[F[_]:Console](clients: Clients[F], clientId: UUID, socket: MessageSocket[F,ClientCommand,ServerCommand])(implicit F: MonadError[F,Throwable]): Stream[F,Nothing] =
    socket
      .read
      .evalMap:
        case ClientCommand.Tick(time) =>
          val log = Stream.exec(Console[F].info(s"server: tick received ${clientId} [$time]"))
          socket.write1(ServerCommand.Tock)
      .drain
