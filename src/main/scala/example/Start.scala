package example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.tamalle.input.DataPipeline.{aCollectPipeline, onlyCSVFiles}
import com.tamalle.input.SinkProvider.listSink
import com.tamalle.input.SourceProvider.{defaultInputPath, listFilesIn}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await

object Start extends App {

  val config = ConfigFactory.parseResources("application.conf")
    .withFallback(ConfigFactory.systemProperties())
    .resolve()

  implicit val system = ActorSystem("collector", config)
  implicit val materializer = ActorMaterializer()

  import scala.concurrent.duration._

  aCollectPipeline()
    .withSource(listFilesIn(defaultInputPath))
    .withSink(listSink)
    .runAfterWarmupOf(1.second)
    .checkEvery(10.minutes)
    .filtering(onlyCSVFiles)
    .run()

  sys.addShutdownHook {
    Await.result(system.whenTerminated, 1.minute)
  }

}
