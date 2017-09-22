package com.transparency.ws

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer {

  private implicit val system: ActorSystem = ActorSystem("my-system")
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val config: Config = ConfigFactory.load()
  private val logger = Logging(system, getClass)

  //Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

  def main(args: Array[String]) {

    val bindingFuture = Http().bindAndHandle(WebServiceControler.routes, config.getString("http.interface"), config.getInt("http.port"))
    val infoMsg = s"WebServer online at http://${config.getString("http.interface")}:${config.getInt("http.port")}/\nPress RETURN to stop..."
    println(infoMsg)
    logger.info(infoMsg)
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
