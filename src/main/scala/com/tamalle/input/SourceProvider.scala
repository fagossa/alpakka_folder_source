package com.tamalle.input

import java.nio.file.{Path, Paths}

import akka.stream.Materializer
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._

object DataPipeline {

  def startPipeline(dataSource: SourceProvider,
                    sink: SinkProvider,
                    interval: FiniteDuration)(implicit mat: Materializer): Future[Seq[Path]] = {
    dataSource
      .listFilesInPath
      .filter {
        _.getFileName.toString.endsWith(".csv")
      }
      .runWith(sink.list[Path])
  }

}

case class SourceProvider(rootPath: Path) {
  val listFilesInPath: Source[Path, NotUsed] = Directory.ls(rootPath)
}

case class SinkProvider() {

  def list[A]: Sink[A, Future[immutable.Seq[A]]] = Sink.seq[A]
}

object SourceProvider {
  def currentPath = new java.io.File(".").getCanonicalPath

  def currentDirectory = Paths.get(s"$currentPath/input")
}

object DataDestination {
  val sink: Sink[Any, Future[Done]] = Sink.foreach(s => println(s))
}


