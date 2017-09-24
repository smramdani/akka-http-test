package com.transparency.ws

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.{ByteString, Timeout}
import com.transparency.db.{DBAccess, DBWork}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.concurrent.duration._

class WebServiceHandlerTest extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfter {

  before {
    // process args if any
    //DBAccess.dropDB
    DBAccess.initDB
  }

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  implicit val timeout: Timeout = Timeout(8.seconds)
  val wrokFetcherActorRef: ActorRef = system.actorOf(Props[DBWork], name = "dbWork")
  val wsHandler: WebServiceHandler = new WebServiceHandler(wrokFetcherActorRef)

  "The service" should {

    "process GET requests to /work/1" in {
      // tests:
      Get("/work/1") ~> wsHandler.routes ~> check {
        responseAs[String] shouldEqual s"""{\"id\":1,\"title\":\"Ma chanson\",\"isrc\":\"1234\",\"iswc\":\"5678\"}"""
      }
    }

    "process GET requests to /work/10" in {
      // tests:
      Get("/work/10") ~> wsHandler.routes ~> check {
        responseAs[String] shouldEqual s"<h1>Unknown ID 10 !</h1>"
      }
    }

    "process POST requests to /work without json data as error" in {
      // tests:
      Post("/works") ~> wsHandler.routes ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    val jsonWorkKeyOk = ByteString(
      s"""
        {
             "isrc": "1234",
             "iswc": "5678"
        }
        """)

    val jsonWorkKeyKo = ByteString(
      s"""
        {
             "isrc": "0000",
             "iswc": "0000"
        }
        """)

    "process POST requests to /work + json data with good keys" in {
      // tests:
      Post("/works", HttpEntity(ContentTypes.`application/json`, jsonWorkKeyOk)) ~> wsHandler.routes ~> check {
        responseAs[String] shouldEqual s"""[{"id":1,\"title\":\"Ma chanson\",\"isrc\":\"1234\",\"iswc\":\"5678\"}]"""
      }
    }

    "process POST requests to /work + json data with wrong keys" in {
      // tests:
      Post("/works", HttpEntity(ContentTypes.`application/json`, jsonWorkKeyKo)) ~> wsHandler.routes ~> check {
        status shouldBe StatusCodes.NoContent
      }
    }

    "process GET requests to /search as Not implemented" in {
      // tests:
      Get("/search") ~> wsHandler.routes ~> check {
        responseAs[String] shouldEqual "<h1>Not Implemented</h1>"
      }
    }

    "process GET requests to /other as 404" in {
      // tests:
      Get("/other") ~> wsHandler.routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }
}
