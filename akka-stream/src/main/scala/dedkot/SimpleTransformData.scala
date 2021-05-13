package dedkot

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.{ Done, NotUsed }

import scala.concurrent.{ ExecutionContextExecutor, Future }

case class BadRecord(name: String, age: Int)
case class GoodRecord(firstName: String, lastName: String, age: Int)

object SimpleTransformData extends App {
  implicit val system: ActorSystem          = ActorSystem("SimpleTransformData")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val source: Source[BadRecord, NotUsed] =
    Source.fromIterator[BadRecord](() =>
      Iterator.from(Seq(BadRecord("John Wick", 60), BadRecord("Tony Stark", 69), BadRecord("Peter Parker", 18)))
    )

  val flow: Flow[BadRecord, GoodRecord, NotUsed] =
    Flow[BadRecord].map { record =>
      val name = record.name.split(" ")
      GoodRecord(
        firstName = name(0),
        lastName = name(1),
        record.age
      )
    }

  val sink: Sink[GoodRecord, Future[Done]] = Sink.foreach[GoodRecord](println)

  source
    .via(flow)
    .runWith(sink)
    .onComplete(_ => system.terminate())
}
