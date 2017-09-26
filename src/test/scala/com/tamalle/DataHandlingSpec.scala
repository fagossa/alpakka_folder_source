package com.tamalle

import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.tamalle.input.DataPipeline.startPipeline
import com.tamalle.input.SourceProvider.currentPath
import com.tamalle.input.{SinkProvider, SourceProvider}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class DataHandlingSpec extends FunSpec
  with Matchers
  with Eventually
  with ScalaFutures
  with IntegrationPatience
  with BeforeAndAfterAll {

  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(5, Millis)))

  private val config = ConfigFactory.parseResources("application.conf")
    .withFallback(ConfigFactory.systemProperties())
    .resolve()

  implicit val system = ActorSystem("mysystem", config)
  implicit val materializer = ActorMaterializer()

  describe("A file collector") {

    def currentDirectory = Paths.get(s"$currentPath/src/test/scala/input")

    it(s"should read files from a folder sink in $currentDirectory") {
      // When
      val future: Future[Seq[Path]] = startPipeline(
        SourceProvider(currentDirectory),
        SinkProvider(),
        1.seconds)
      // Then
      whenReady(future) { r => r.map(_.getFileName.toString).toList shouldBe List("sample.csv") }
    }
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    Await.result(system.terminate(), 2.seconds)
  }
}
