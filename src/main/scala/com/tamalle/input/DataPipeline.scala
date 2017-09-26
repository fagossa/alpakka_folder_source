package com.tamalle.input

import java.nio.file.{Path, Paths}

import akka.event.Logging
import akka.stream.{Attributes, Materializer}
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._

object DataPipeline {

  case class PipelineBuilder[A, B](source: Source[A, NotUsed],
                                   sink: Sink[A, Future[B]],
                                   warmup: FiniteDuration,
                                   interval: FiniteDuration,
                                   filter: Flow[A, A, NotUsed]) {

    def checkEvery(newInterval: FiniteDuration): PipelineBuilder[A, B] = copy(interval = newInterval)

    def runAfterWarmupOf(newWarmup: FiniteDuration): PipelineBuilder[A, B] = copy(warmup = newWarmup)

    def withSink(newSink: Sink[A, Future[B]]): PipelineBuilder[A, B] = copy(sink = newSink)

    def withSource(newSource: Source[A, NotUsed]): PipelineBuilder[A, B] = copy(source = newSource)

    def filtering(newFilter: Flow[A, A, NotUsed]): PipelineBuilder[A, B] = copy(filter = newFilter)

    def run()(implicit mat: Materializer): Future[B] =
      Source
        .tick(warmup, interval, None).named("Tick")
        .flatMapConcat(_ => source)
        .log("Checking input data one more time")
        .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel))
        .via(filter)
        .runWith(sink)

  }

  def aCollectPipeline() = PipelineBuilder(
    source = Source.empty[Path],
    sink = Sink.seq[Path],
    filter = Flow[Path],
    warmup = 0.seconds,
    interval = 0.seconds
  )

  val onlyCSVFiles: Flow[Path, Path, NotUsed] =
    Flow[Path]
      .collect {
        case p: Path if p.getFileName.toString.endsWith(".csv") => p
      }.log("Filtering files")
}

object SinkProvider {
  def listSink[A]: Sink[A, Future[immutable.Seq[A]]] = Sink.seq[A]
  val printSink: Sink[Any, Future[Done]] = Sink.foreach(s => println(s))
}

object SourceProvider {
  def currentPath = new java.io.File(".").getCanonicalPath

  def defaultInputPath = Paths.get(s"$currentPath/input")

  def listFilesIn(rootPath: Path): Source[Path, NotUsed] = Directory.ls(rootPath)
}
