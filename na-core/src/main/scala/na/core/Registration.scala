package na.core

import PID.*
import Copy.*

case class Registration(copies: Map[Copy,PID]):

  lazy val externalPID: PID =
    copies.getOrElse(External, sys.error("no external copy"))

  lazy val internalPIDs: List[PID] =
    import cats.implicits.*
    List(copies.get(Internal1), copies.get(Internal2), copies.get(Internal3)).sequence.getOrElse(List.empty[PID])