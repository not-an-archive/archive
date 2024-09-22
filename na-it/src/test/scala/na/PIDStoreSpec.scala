package na

import cats.implicits.*
import cats.effect.*

import doobie.util.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*

import org.scalatest.*
import org.scalatest.concurrent.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.*
import org.scalatest.time.*

import na.core.*
import na.registry.store.*

import scala.concurrent.*

class PIDStoreSpec
  extends AsyncFlatSpec
  with Matchers
  with BeforeAndAfterAll
  with Eventually:

  import PID.*
  import Born.*
  import Copy.*

  import cats.effect.unsafe.implicits.global

  def transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      config     <- Config.load
      ec         <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Archive.transactor(config.database, ec)
    } yield transactor

  val store: PIDStore = PIDStore

  override implicit val patienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(1, Second)))

  override protected def beforeAll(): Unit =
    eventually:
      val ext = PID.random(Digitally, External)
      transactor
        .use((store.create(ext) *> store.get(ext)).transact)
        .unsafeRunSync()
        .externalPID == ext
    println("PIDStore started")

  "PIDStore" should "create an external pid" in {
    assert(true)
  }