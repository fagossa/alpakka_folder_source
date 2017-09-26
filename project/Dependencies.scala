import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val alpakka = "com.lightbend.akka" %% "akka-stream-alpakka-file" % "0.12" withSources()
}
