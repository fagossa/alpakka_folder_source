package example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.tamalle.input.DataPipeline.startPipeline
import com.tamalle.input.{SinkProvider, SourceProvider}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await

object Start extends Greeting with App {

  val config = ConfigFactory.parseResources("application.conf")
    .withFallback(ConfigFactory.systemProperties())
    .resolve()

  implicit val system = ActorSystem("concertGateway", config)
  implicit val materializer = ActorMaterializer()

  import scala.concurrent.duration._

  val response = startPipeline(
    new SourceProvider(SourceProvider.currentDirectory),
    SinkProvider(),
    3.seconds)

  sys.addShutdownHook {
    Await.result(system.whenTerminated, 1.second)
  }

}

trait Greeting {
  lazy val greeting: String = "hello"
}
