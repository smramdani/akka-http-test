package com.transparency.ws

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContext, Future}
//import com.typesafe.config.ConfigFactory

object WebServiceControler {
  type Works = List[Work]
  val routes: Route = {
    (get & path("search")) {
      logger.debug("GET /search")
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Search Works</h1>"))
    } ~ (get & path("work")) {
      logger.debug("GET /work")
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Get work</h1>"))
    } ~ (post & path("work")) {
      logger.debug("POST /work")
      entity(as[WorkKey]) { workKey =>
        val worksFound: Future[Option[Works]] = fetchWorks(workKey)
        onSuccess(worksFound) {
          case Some(works) => complete(works)
          case None => complete(StatusCodes.NoContent)
        }
      } ~ {
        complete(StatusCodes.BadRequest)
      }
    }
  }
  //val config = ConfigFactory.load()
  private val logger: LoggingAdapter = Logging(system, getClass)

  // formats for unmarshalling and marshalling
  implicit val workKeyFormat: RootJsonFormat[WorkKey] = jsonFormat2(WorkKey)
  implicit val workFormat: RootJsonFormat[Work] = jsonFormat3(Work)

  private implicit val system: ActorSystem = ActorSystem("my-system")

  // Fake function in place of SQL request implementation
  def fetchWorks(workKey: WorkKey): Future[Option[Works]] = Future {
    // Fake code for test
    if (workKey.isrc == "1234" || workKey.iswc == "5678")
      Some(List(Work("Ma chanson", "1234", "5678")))
    else
      None
  }(ExecutionContext.global)

  // domain model
  final case class WorkKey(isrc: String, iswc: String)

  final case class Work(title: String, isrc: String, iswc: String)

}
