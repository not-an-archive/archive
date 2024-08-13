package org

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

import na.nl.*

class OrganisationServerSpec
  extends AsyncFlatSpec
  with Checkpoints
  with Matchers
  with BeforeAndAfterAll
  with Eventually:

  import cats.effect.unsafe.implicits.global

  val server: IO[ExitCode] =
    OrganisationServer.create

  var shutdown: () => Future[Unit] =
    println("Start OrganisationServer..")
    server.unsafeRunCancelable()

  override implicit val patienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(1, Second)))

  override protected def beforeAll(): Unit =
    eventually(client.use(_.expect[Json](endpoint)).unsafeRunSync())
    println("OrganisationServer started")

  override protected def afterAll(): Unit =
    import scala.concurrent.duration.*
    println("Stopping OrganisationServer..")
    Await.result(shutdown(), 10.seconds)
    println("OrganisationServer stopped.")

  lazy val client: Resource[IO, org.http4s.client.Client[IO]] =
    org.http4s.ember.client.EmberClientBuilder.default[IO].build

  val config: Config =
    Config.load.use(IO.pure).unsafeRunSync()

  val endpoint: Uri =
    Uri.unsafeFromString(s"http://${config.server.host}:${config.server.port}/organisations")

  def asyncJsonFrom(request: Request[IO]): Json =
    client.use(_.expect[Json](request)).unsafeRunSync()

  "OrganisationServer" should "create a organisation" in {

      val name = "create organisation"
      val entity =
        json"""{
          "name": $name
        }"""

      val json = asyncJsonFrom(POST(endpoint).withEntity(entity)).hcursor

      val cp = new Checkpoint
      cp { json.downField("id").as[Identity].isRight   === true }
      cp { json.downField("name").as[String]    === Right(name) }
      cp.reportAll()

      Succeeded
  }

  it should "update a organisation" in {
      val id = createOrganisation("my organisation 2")

      val name = "updated organisation"
      val updateJson =
        json"""{
          "name": $name
        }"""

      asyncJsonFrom(PUT(endpoint / id).withEntity(updateJson)) shouldBe json"""
        {
          "id": $id,
          "name": $name
        }"""
  }

  it should "return a single organisation" in {
      val name = "my organisation 3"
      val id = createOrganisation(name)

      client.use(_.expect[Json](endpoint / id)).unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": $name
        }"""
  }

  it should "delete a organisation" in {
      val name = "my organisation 4"
      val id = createOrganisation(name)

      client.use(_.status(DELETE(endpoint / id))).unsafeRunSync() shouldBe Status.NoContent
      client.use(_.status(GET(endpoint / id))).unsafeRunSync()    shouldBe Status.NotFound
  }

  it should "return all organisations" in {
    // Remove all existing organisations
    val json = client.use(_.expect[Json](endpoint)).unsafeRunSync()
    json.hcursor.as[List[Organisation]].foreach(_.foreach(c =>
      client.use(_.status(DELETE(endpoint / c.id.get))).unsafeRunSync() shouldBe Status.NoContent
    ))

    // Add new organisations
    val name1 = "my organisation 1"
    val name2 = "my organisation 2"
    val id1 = createOrganisation(name1)
    val id2 = createOrganisation(name2)

    // Retrieve organisations
    client.use(_.expect[Json](endpoint)).unsafeRunSync() shouldBe json"""
      [
        {
          "id": $id1,
          "name": $name1
        },
        {
          "id": $id2,
          "name": $name2
        }
      ]"""
  }

  private def createOrganisation(name: String): String =
    val createJson = json"""
      {
        "name": $name
      }"""
    val request = Request[IO](method = Method.POST, uri = endpoint).withEntity(createJson)
    val id = asyncJsonFrom(request).hcursor.downField("id").as[Identity]
    id.getOrElse(sys.error(s"error creating: ${createJson.spaces2}\n$id}")).toString
