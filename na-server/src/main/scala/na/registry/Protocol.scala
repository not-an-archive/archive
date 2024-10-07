package na.registry

/** Defines the messages exchanged between the client and server. */
object Protocol:

  import scodec.*
  import scodec.codecs.*

  /** we measure time as specified [[System.nanoTime]]*/
  private val time: Codec[Long] =
    codecs.int64

  /** Base trait for messages sent from the client to the server. */
  enum ClientCommand:
    case Tick(local: Long)
    // case Disconnect

  object ClientCommand:
    val codec: Codec[ClientCommand] =
      discriminated[ClientCommand]
        .by(uint8)
        .typecase(1, time.as[Tick])

/** Base trait for messages sent from the server to the client. */
  enum ServerCommand:
    case Tock
  object ServerCommand:
    val codec: Codec[ServerCommand] =
      discriminated[ServerCommand]
        .by(uint8)
        .typecase(129, provide(Tock))
