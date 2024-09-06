package na

import org.scalatest.*
import org.scalatest.concurrent.*
import org.scalatest.matchers.should.*
import org.scalatest.time.*
import org.scalatest.flatspec.*
import cats.effect.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.dsl.io.*
import org.http4s.dsl.io.*

import scala.concurrent.*
import io.circe.*
import io.circe.literal.*
import na.*
import core.*
import organisation.*

class OrganisationServiceSpec
  extends AsyncFlatSpec
  with Checkpoints
  with Matchers
  with BeforeAndAfterAll
  with Eventually:

  import cats.effect.unsafe.implicits.global

  val server: IO[ExitCode] =
    Server.create

  var shutdown: () => Future[Unit] =
    println("Start Server..")
    server.unsafeRunCancelable()

  override implicit val patienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(1, Second)))

  override protected def beforeAll(): Unit =
    eventually(client.use(_.expect[Json](endpoint)).unsafeRunSync())
    println("Server started")

  override protected def afterAll(): Unit =
    import scala.concurrent.duration.*
    println("Stopping Server..")
    Await.result(shutdown(), 10.seconds)
    println("Server stopped.")

  lazy val client: Resource[IO, org.http4s.client.Client[IO]] =
    org.http4s.ember.client.EmberClientBuilder.default[IO].build

  val config: Config =
    Config.load.use(IO.pure).unsafeRunSync()

  val endpoint: Uri =
    Uri.unsafeFromString(s"http://${config.server.host}:${config.server.port}/organisations")

  def asyncJsonFrom(request: Request[IO]): Json =
    client.use(_.expect[Json](request)).unsafeRunSync()

  "Server" should "create a organisation" in {

      val name = "create organisation"
      val entity =
        json"""{
          "name": $name
        }"""

      val json = asyncJsonFrom(POST(endpoint).withEntity(entity)).hcursor

      val cp = new Checkpoint
      cp { json.downField("id").as[PID].isRight === true }
      cp { json.downField("name").as[String]    === Right(name) }
      cp.reportAll()

      Succeeded
  }

  it should "update a organisation" in {
      val pid = createOrganisation("my organisation 2")

      val name = "updated organisation"
      val updateJson =
        json"""{
          "name": $name
        }"""

      asyncJsonFrom(PUT(endpoint / pid).withEntity(updateJson)) shouldBe json"""
        {
          "pid": ${pid},
          "name": $name
        }"""
  }

  it should "return a single organisation" in {
      val name = "my organisation 3"
      val pid = createOrganisation(name)

      client.use(_.expect[Json](endpoint / pid)).unsafeRunSync() shouldBe json"""
        {
          "pid": ${pid},
          "name": $name
        }"""
  }

  it should "delete a organisation" in {
      val name = "my organisation 4"
      val pid = createOrganisation(name)

      client.use(_.status(DELETE(endpoint / pid))).unsafeRunSync() shouldBe Status.NoContent
      client.use(_.status(GET(endpoint / pid))).unsafeRunSync()    shouldBe Status.NotFound
  }

  it should "return all organisations" in {
    // Remove all existing organisations
    val json = client.use(_.expect[Json](endpoint)).unsafeRunSync()
    json.hcursor.as[List[Organisation]].foreach(_.foreach(c =>
      client.use(_.status(DELETE(endpoint / c.pid.get))).unsafeRunSync() shouldBe Status.NoContent
    ))

    // Add new organisations
    val name1 = "my organisation 1"
    val name2 = "my organisation 2"
    val pid1 = createOrganisation(name1)
    val pid2 = createOrganisation(name2)

    // Retrieve organisations
    client.use(_.expect[Json](endpoint)).unsafeRunSync() shouldBe json"""
      [
        {
          "pid": $pid1,
          "name": $name1
        },
        {
          "pid": $pid2,
          "name": $name2
        }
      ]"""
  }

  private def createOrganisation(name: String): PID =
    val createJson = json"""
      {
        "name": $name
      }"""
    val request = Request[IO](method = Method.POST, uri = endpoint).withEntity(createJson)
    val pid = asyncJsonFrom(request).hcursor.downField("pid").as[PID]
    pid.getOrElse(sys.error(s"error creating: ${createJson.spaces2}\n$pid}"))
