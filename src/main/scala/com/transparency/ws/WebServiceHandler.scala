package com.transparency.ws

import akka.actor.ActorRef
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.transparency.domain.{Work, WorkKey}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.duration._

class WebServiceHandler(val workFetcherActorRef: ActorRef) {

  // formats for unmarshalling and marshalling
  implicit val workKeyFormat: RootJsonFormat[WorkKey] = jsonFormat2(WorkKey)
  implicit val workFormat: RootJsonFormat[Work] = jsonFormat4(Work)

  private val logger: LoggingAdapter = Logging(AppConfig.system, getClass)

  type Works = List[Work]

  private val homePage =
    s"""
       | <html>
       |  <body>
       |    <h1>Coucou Naël ;)</h1>
       |    <h2>Lister les chansons</h2>
       |    <a href="/works">Toutes les chansons</a>
       |  </body>
       | </html>
     """.stripMargin

  val routes: Route = {

    implicit val timeout: Timeout = Timeout(3.seconds)

    // route : GET /search
    (get & pathSingleSlash) {
      logger.debug("Request => GET /")
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, homePage))

      // route : GET /work
    } ~ (get & path("work" / IntNumber)) { workId =>
      logger.debug("Request => GET /work")
      val worksFound = (workFetcherActorRef ? workId).mapTo[Option[Work]]
      onSuccess(worksFound) {
        case None => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Unknown ID $workId !</h1>")) //StatusCodes.NoContent)
        case Some(work: Work) => complete(work)
      }

      // route : GET /works
    } ~ (get & path("works")) {
      logger.debug("Request => GET /works")
      val worksFound = (workFetcherActorRef ? "tout").mapTo[Works]
      onSuccess(worksFound) {
        case Nil => complete(StatusCodes.NoContent)
        case works: Works => complete(works)
      }

      // route : POST /work
    } ~ (post & path("works")) {
      logger.debug("Request => POST /works")
      entity(as[WorkKey]) { workKey =>
        val worksFound = (workFetcherActorRef ? workKey).mapTo[Works]
        onSuccess(worksFound) {
          case Nil => complete(StatusCodes.NoContent)
          case works: Works => complete(works)
        }
      } ~ {
        complete(StatusCodes.BadRequest)
      }
      // route : GET /search
    } ~ (get & path("search")) {
      logger.debug("Request => GET /search")
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Not Implemented</h1>"))
    } ~ {
      complete(StatusCodes.NotFound)
    }
  }
}
