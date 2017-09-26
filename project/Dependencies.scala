import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val alpakka = "com.lightbend.akka" %% "akka-stream-alpakka-file" % "0.12" withSources()
  lazy val akkaLog =  "com.typesafe.akka" %% "akka-slf4j" % "2.5.4"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.13"
}
