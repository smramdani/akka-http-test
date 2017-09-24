package com.transparency.ws

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.transparency.db.{DBAccess, DBWork}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer {

  private implicit val system: ActorSystem = AppConfig.system
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val logger = Logging(system, logSource = getClass)

  def initDB(): Unit = {

    DBAccess.dropDB
    DBAccess.initDB
  }

  def startServices() {

    val httpInterface: String = AppConfig.httpInterface
    val httpPort: Int = AppConfig.httpPort

    val wrokFetcherActorRef = system.actorOf(Props[DBWork], name = "dbWork")
    val wsControler = new WebServiceHandler(wrokFetcherActorRef)

    val bindingFuture = Http().bindAndHandle(wsControler.routes, httpInterface, httpPort)
    val infoMsg = s"WebServer online at http://$httpInterface:$httpPort/\nPress RETURN to stop..."
    println(infoMsg)
    logger.info(infoMsg)
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => AppConfig.system.terminate()) // and shutdown when done
  }

  def main(args: Array[String]) {

    // process args if any

    // init DB
    initDB()

    // start Services
    WebServer.startServices()
  }
}
