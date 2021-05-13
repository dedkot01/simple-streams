package dedkot

import akka.actor.ActorSystem
import akka.stream.ClosedShape
import akka.stream.alpakka.csv.scaladsl.{ CsvParsing, CsvToMap }
import akka.stream.alpakka.file.DirectoryChange
import akka.stream.alpakka.file.scaladsl.{ Directory, DirectoryChangesSource }
import akka.stream.scaladsl.{ FileIO, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source }
import akka.{ Done, NotUsed }

import java.nio.charset.StandardCharsets
import java.nio.file.{ FileSystems, Path }
import scala.concurrent.duration.DurationInt
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

object SimpleGraphFanIn2 extends App {
  implicit val system: ActorSystem = ActorSystem("Graph")

  RunnableGraph
    .fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val source1 = Source(1 to 10)
      val source2 = Source(11 to 20)
      val merge   = builder.add(Merge[Int](2))
      val sink    = Sink.foreach(println)

      source1 ~> merge ~> sink
      source2 ~> merge

      ClosedShape
    })
    .run()
}

object SimpleTransformDataFromFilesInDirectory extends App {
  implicit val system: ActorSystem          = ActorSystem("SimpleTransformDataFromFile")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val path = FileSystems.getDefault.getPath(".\\test-data")

  val directory: Source[Path, NotUsed] = Directory.ls(path)
  val directoryChangesCreation: Source[Path, NotUsed] = DirectoryChangesSource(path, 1.second, 1000).collect {
    case (path, DirectoryChange.Creation) => path
  }
  val mergedSource: Source[Path, NotUsed] =
    Source.combine(directory, directoryChangesCreation)(Merge[Path](_))

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

  mergedSource.via(files).via(records).via(transform).runForeach(println)
}
