import org.apache.spark.{ SparkConf, SparkContext }

object SimpleApp extends App {
  val conf = new SparkConf().setAppName("SimpleApp").setMaster("local")
  val sc   = new SparkContext(conf)

  val file = sc.textFile(raw"C:\Users\dzhdanov\IdeaProjects\simple-streams\spark-stream\src\main\resources\text.txt")
  file.map(s => s.length).foreach(println)

  sc.stop()
}
