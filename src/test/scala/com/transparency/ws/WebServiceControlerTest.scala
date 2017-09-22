package com.transparency.ws

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.{Matchers, WordSpec}

class WebServiceControlerTest extends WordSpec with Matchers with ScalatestRouteTest {

  "The service" should {

    "return a '<h1>Search Works</h1>' response for GET requests to /search" in {
      // tests:
      Get("/search") ~> WebServiceControler.routes ~> check {
        responseAs[String] shouldEqual "<h1>Search Works</h1>"
      }
    }

    "return a '<h1>Get work</h1>' response for GET requests to /work" in {
      // tests:
      Get("/work") ~> WebServiceControler.routes ~> check {
        responseAs[String] shouldEqual "<h1>Get work</h1>"
      }
    }

    "return BadRequest for POST requests to the root /work without json data" in {
      // tests:
      Post("/work") ~> WebServiceControler.routes ~> check {
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

    "return a found work for POST requests to the root /work + json data" in {
      // tests:
      Post("/work", HttpEntity(ContentTypes.`application/json`, jsonWorkKeyOk)) ~> WebServiceControler.routes ~> check {
        responseAs[String] shouldEqual s"""[{\"title\":\"Ma chanson\",\"isrc\":\"1234\",\"iswc\":\"5678\"}]"""
      }
    }

    "return a not found message for POST requests to the root /work + json data with wrong key" in {
      // tests:
      Post("/work", HttpEntity(ContentTypes.`application/json`, jsonWorkKeyKo)) ~> WebServiceControler.routes ~> check {
        status shouldBe StatusCodes.NoContent
      }
    }
  }
}
