package dedkot

import org.apache.flink.streaming.api.scala._

object WordCountFromFile extends App {
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val text = env.readTextFile(
    "C:\\Users\\dzhdanov\\IdeaProjects\\simple-streams\\flink-stream\\src\\main\\resources\\test.txt"
  )

  val counts = text.flatMap {
    _.toLowerCase.split("\\W+") filter (_.nonEmpty)
  }.map(_ -> 1)
    .keyBy(_._1)
    .sum(1)

  counts.print()

  env.execute("Window Stream WordCount")
}
