package dedkot

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{ FileIO, Source }
import akka.util.ByteString
import akka.{ Done, NotUsed }

import java.nio.file.Paths
import scala.concurrent.{ ExecutionContextExecutor, Future }

object SimpleSourceExample extends App {
  implicit val system: ActorSystem          = ActorSystem("QuickStart")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val done: Future[Done]           = source.runForeach(println)
  done.onComplete(_ => system.terminate())
}

object FactorialsInFileExample extends App {
  implicit val system: ActorSystem          = ActorSystem("QuickStart")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val source     = Source(1 to 100)
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
  val result: Future[IOResult] = {
    factorials.map(num => ByteString(s"$num\n")).runWith(FileIO.toPath(Paths.get("factorials.txt")))
  }
  result.onComplete(_ => system.terminate())
}
