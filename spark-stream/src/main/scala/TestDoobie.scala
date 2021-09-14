import cats.effect._
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

import scala.concurrent.ExecutionContext

case class SubscriptionDataForSpark(startDate: Int, endDate: Int, duration: Int, price: Double)

object TestDoobie {

  def main(args: Array[String]): Unit = {
    insertData(
      SubscriptionDataForSpark(
        1,
        1,
        1,
        1.0
      ),
      "subscription"
    )
    println("H")
  }

  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/postgres",
    "postgres", // user
    "postgres"  // password
  )

  def insertData(data: SubscriptionDataForSpark, tableName: String): Unit = {
    sql"""
        INSERT INTO subscription ("startDate", "endDate", duration, price) VALUES
        (${data.startDate}, ${data.endDate}, ${data.duration}, ${data.price})
      """.update.run.transact(xa).unsafeRunSync
  }

}
