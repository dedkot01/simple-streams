package dedkot

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.scala.{ createTypeInformation, StreamExecutionEnvironment }
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

object WindowStream extends App {
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val numbers = Seq(1 -> 4, 1 -> 6, 2 -> 1, 3 -> 8, 1 -> 2, 3 -> 1, 2 -> 10)

  env
    .fromCollection(numbers)
    .ass
    .keyBy(_._1)
    .window(TumblingEventTimeWindows.of(Time.seconds(5)))
    .reduce { (v1, v2) =>
      (v1._1, v1._2 + v2._2)
    }
    .print

  env.execute("WindowStream")
}
