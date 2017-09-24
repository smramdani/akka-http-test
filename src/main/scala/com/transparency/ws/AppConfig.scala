package com.transparency.ws

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

  val system: ActorSystem = ActorSystem("my-system")
  val config: Config = ConfigFactory.load()

  val httpInterface: String = config.getString("http.interface")
  val httpPort: Int = config.getInt("http.port")
}
