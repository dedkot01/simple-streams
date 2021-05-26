package dedkot

import org.apache.flink.api.common.state.{ ValueState, ValueStateDescriptor }
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.streaming.api.scala.{ createTypeInformation, StreamExecutionEnvironment }
import org.apache.flink.util.Collector

object CoFunctionsStream extends App {
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val positiveNumbers = Seq(1 -> 2, 2  -> 6, 1 -> 5, 2 -> 18)
  val negativeNumbers = Seq(1 -> -1, 3 -> -10)

  val mustBe = Seq(1 -> CounterGoodNumbers(3, 2), 2 -> CounterGoodNumbers(2, 2))

  val negativeNumbersStream = env
    .fromCollection(negativeNumbers)
    .keyBy(_._1)

  val positiveNumbersStream = env
    .fromCollection(positiveNumbers)
    .keyBy(_._1)
    .connect(negativeNumbersStream)
    .process(new CounterGoodNumbersFunction)

  val aggregateStream = env
    .fromCollection(mustBe)
    .keyBy(_._1)
    .connect(positiveNumbersStream)
    .process(new AggregateProcessFunction)
    .print

  env.execute("counter good numbers")
}

case class CounterGoodNumbers(size: Int, countGoodNumbers: Int)

class CounterGoodNumbersFunction extends CoProcessFunction[(Int, Int), (Int, Int), CounterGoodNumbers] {

  lazy val state: ValueState[CounterGoodNumbers] = getRuntimeContext.getState(
    new ValueStateDescriptor[CounterGoodNumbers]("state counter", classOf[CounterGoodNumbers])
  )

  override def processElement1(
    value: (Int, Int),
    ctx: CoProcessFunction[(Int, Int), (Int, Int), CounterGoodNumbers]#Context,
    out: Collector[CounterGoodNumbers]
  ): Unit = {
    val currentState = Option(state.value())
    currentState match {
      case None =>
        state.update(CounterGoodNumbers(1, 1))
      case Some(currentState) =>
        state.update(CounterGoodNumbers(currentState.size + 1, currentState.countGoodNumbers + 1))
    }
    out.collect(state.value())
  }

  // negative
  override def processElement2(
    value: (Int, Int),
    ctx: CoProcessFunction[(Int, Int), (Int, Int), CounterGoodNumbers]#Context,
    out: Collector[CounterGoodNumbers]
  ): Unit = {
    val currentState = Option(state.value())
    currentState match {
      case None =>
        state.update(CounterGoodNumbers(1, 0))
      case Some(currentState) =>
        state.update(CounterGoodNumbers(currentState.size + 1, currentState.countGoodNumbers))
    }
    out.collect(state.value())
  }

}

class AggregateProcessFunction
    extends CoProcessFunction[(Int, CounterGoodNumbers), CounterGoodNumbers, CounterGoodNumbers] {

  lazy val stateFromMustBe: ValueState[(Int, CounterGoodNumbers)] = getRuntimeContext.getState(
    new ValueStateDescriptor[(Int, CounterGoodNumbers)]("aggregate state", classOf[(Int, CounterGoodNumbers)])
  )

  lazy val stateFromCounter: ValueState[CounterGoodNumbers] = getRuntimeContext.getState(
    new ValueStateDescriptor[CounterGoodNumbers]("aggregate counter state", classOf[CounterGoodNumbers])
  )

  override def processElement1(
    value: (Int, CounterGoodNumbers),
    ctx: CoProcessFunction[(Int, CounterGoodNumbers), CounterGoodNumbers, CounterGoodNumbers]#Context,
    out: Collector[CounterGoodNumbers]
  ): Unit = {
    stateFromMustBe.update(value)
    val state = Option(stateFromCounter.value())
    state match {
      case Some(currentState) =>
        if (currentState == value._2) {
          out.collect(value._2)
          stateFromCounter.clear()
          stateFromMustBe.clear()
        }
      case None => Unit
    }
  }

  override def processElement2(
    value: CounterGoodNumbers,
    ctx: CoProcessFunction[(Int, CounterGoodNumbers), CounterGoodNumbers, CounterGoodNumbers]#Context,
    out: Collector[CounterGoodNumbers]
  ): Unit = {
    stateFromCounter.update(value)
    val currentState = Option(stateFromMustBe.value())
    currentState match {
      case Some(currentState) =>
        if (currentState._2 == value) {
          out.collect(value)
          stateFromMustBe.clear()
          stateFromCounter.clear()
        }
    }
  }

}
