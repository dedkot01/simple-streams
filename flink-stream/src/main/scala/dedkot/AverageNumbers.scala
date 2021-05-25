package dedkot

import org.apache.flink.streaming.api.scala.{ createTypeInformation, StreamExecutionEnvironment }
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

object AverageNumbers extends App {
  val nums = Seq(1L -> 2L, 2L -> 6L, 1L -> 4L, 2L -> 4L, 3L -> 4L)

  val env = StreamExecutionEnvironment.getExecutionEnvironment

  env
    .fromCollection(nums)
    .keyBy(_._1)
    .mapWithState { (in: (Long, Long), average: Option[Long]) =>
      average match {
        case Some(currentAverage) =>
          ((in._1, (currentAverage + in._2) / 2), Some((currentAverage + in._2) / 2))
        case None => ((in._1, in._2), Some(in._2))
      }
    }
    .print

  env.execute("ExampleKeyedState")
}
