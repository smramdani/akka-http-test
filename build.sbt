name := "transparency"

version := "0.1"

scalaVersion := "2.12.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.5.4"
  val akkaHttpV = "10.0.10"
  val scalaTestV = "3.0.1"
  val slickV = "3.2.1"
  val slf4jV = "1.6.4"
  val h2DatabaseV = "1.4.196"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-typed" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "com.typesafe.slick" %% "slick" % slickV,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    //"org.slf4j" % "slf4j-logback" % slf4jV,
    "com.h2database" % "h2" % h2DatabaseV
  )
}

//Revolver.settings
