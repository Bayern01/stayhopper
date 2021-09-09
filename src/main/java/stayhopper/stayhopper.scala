package stayhopper

import com.graphhopper.matching.Observation
import com.graphhopper.util.shapes.GHPoint
import org.apache.spark.{SparkConf, SparkContext}

object stayhopper {
  def main(args: Array[String]): Unit = {
    println("*********start**********")
    //val conf = new SparkConf().set("spark.hadoop.mapreduce.output.fileoutputformat.compress", "false").setAppName("driver")
    val conf = new SparkConf().setMaster("local").setAppName("SparkHiveText")
    val sc = new SparkContext(conf)

    println("******before sc.broadcast**********")

    val relpath = System.getenv("SPARK_YARN_STAGING_DIR")

    val config = "hdfs://127.0.0.1:9000/target/sichuan.osm.pbf"

    println(config)
    val matcher = sc.broadcast(new BroadcastMatcher.BroadcastMatcher(config))

    println("*****after sc.broadcast******")

    val traces = sc.textFile("file:///d:/input/user.txt").map(f = x => {
      val y = x.split(",")
      (y(0), y(1), y(2).toDouble, y(3).toDouble)
    })

    traces.foreach(println)

    println("traces.count = ", traces.count())

    val routePoint = new java.util.ArrayList[Observation]()

    val matches = traces.groupBy(x => x._1).map(x => {
      val trip = x._2.map({
        x => new Observation(new GHPoint(x._4.toDouble,x._3.toDouble))
      }).toList
      matcher.value.mmatch(trip)
    })

    println("matches.count = " + matches.count())

  }

}
