package dedkot

import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.{ CsvParsing, CsvToMap }
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl.{ FileIO, Flow, Sink, Source }
import akka.{ Done, NotUsed }

import java.nio.charset.StandardCharsets
import java.nio.file.{ FileSystems, Path }
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

object SimpleTransformDataFromFile extends App {
  implicit val system: ActorSystem          = ActorSystem("SimpleTransformDataFromFile")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val path = FileSystems.getDefault.getPath(".\\test-data")

  val directory: Source[Path, NotUsed] = Directory.ls(path)
  val files: Flow[Path, Map[String, String], NotUsed] = Flow[Path].flatMapConcat { path =>
    FileIO.fromPath(path).via(CsvParsing.lineScanner()).via(CsvToMap.toMapAsStrings(StandardCharsets.UTF_8))
  }
  val records: Flow[Map[String, String], BadRecord, NotUsed] = Flow[Map[String, String]].map { record =>
    BadRecord(record("name"), record("age").toInt)
  }
  val transform: Flow[BadRecord, GoodRecord, NotUsed] = Flow[BadRecord].map { record =>
    val names = record.name.split(" ")
    GoodRecord(names(0), names(1), record.age)
  }

  directory.via(files).via(records).via(transform).runForeach(println)
}
