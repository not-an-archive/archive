package naa

object stream:

    import fs2.Stream

    extension [F[_], A](fa: F[A]) def stream: Stream[F, A] = Stream.eval(fa)
