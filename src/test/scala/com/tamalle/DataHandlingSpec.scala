package com.tamalle

import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.tamalle.input.DataPipeline.onlyCSVFiles
import com.tamalle.input.SourceProvider.currentPath
import com.tamalle.input.{SinkProvider, SourceProvider}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class DataHandlingSpec extends FunSpec
  with Matchers
  with Eventually
  with ScalaFutures
  with IntegrationPatience
  with BeforeAndAfterAll {

  private val config = ConfigFactory.parseResources("application.conf")
    .withFallback(ConfigFactory.systemProperties())
    .resolve()

  implicit val system = ActorSystem("mysystem", config)
  implicit val materializer = ActorMaterializer()

  describe("A file collector") {

    def currentDirectory = Paths.get(s"$currentPath/src/test/scala/input")

    it(s"should filter files from a folder sink in $currentDirectory") {
      // Given
      val aSource = SourceProvider.listFilesIn(currentDirectory)
      val aSink = SinkProvider.listSink[Path]
      // When
      val future: Future[Seq[Path]] =
        aSource
          .via(onlyCSVFiles)
          .runWith(aSink)
      // Then
      whenReady(future) {
        _.map(_.getFileName.toString).toList shouldBe List("sample.csv")
      }
    }
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    Await.result(system.terminate(), 2.seconds)
  }
}
