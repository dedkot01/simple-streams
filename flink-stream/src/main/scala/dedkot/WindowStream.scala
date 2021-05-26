package dedkot

import org.apache.flink.api.common.eventtime.{ SerializableTimestampAssigner, WatermarkStrategy }
import org.apache.flink.streaming.api.scala.{ createTypeInformation, StreamExecutionEnvironment }
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

import java.time.Duration

object WindowStream extends App {
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val numbers = Seq(1 -> 4, 1 -> 6, 2 -> 1, 3 -> 8, 1 -> 2, 3 -> 1, 2 -> 10)

  env
    .fromCollection(numbers)
    .assignTimestampsAndWatermarks(
      WatermarkStrategy
        .forBoundedOutOfOrderness[(Int, Int)](Duration.ofSeconds(15))
        .withTimestampAssigner(new SerializableTimestampAssigner[(Int, Int)] {
          override def extractTimestamp(element: (Int, Int), recordTimestamp: Long): Long = element._1
        })
    )
    .keyBy(_._1)
    .window(TumblingEventTimeWindows.of(Time.seconds(15)))
    .reduce { (v1, v2) =>
      (v1._1, v1._2 + v2._2)
    }
    .print

  env.execute("WindowStream")
}
