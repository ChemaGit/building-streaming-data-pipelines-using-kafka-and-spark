package structuredstreamingdemo

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession

import org.apache.spark.sql.functions.{split, to_timestamp, window, count}
import org.apache.spark.sql.streaming.Trigger

object GetStreamingDepartmentTraffic {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load.getConfig(args(0))
    val spark = SparkSession.
      builder.
      master(conf.getString("execution.mode")).
      appName("Get Streaming Department Traffic").
      getOrCreate()

    import spark.implicits._
    spark.sparkContext.setLogLevel("ERROR")
    spark.conf.set("spark.sql.shuffle.partitions", "2")

    val lines = spark.
      readStream.
      format("socket").
      option("host", conf.getString("data.host")).
      option("port", conf.getString("data.port")).
      load

    /*val departmentLines = lines.
      filter(split(split($"value", " ")(6), "/")(1) === "department").
      withColumn("visit_time", to_timestamp(split($"value", " ")(3), "[dd/MMM/yyyy:HH:mm:ss")).
      withColumn("department_name", split(split($"value", " ")(6), "/")(2)).
      drop($"value")

    val departmentTraffic = departmentLines.
      groupBy(
        window($"visit_time", "60 seconds", "20 seconds"),
        $"department_name"
      ).
      agg(count("visit_time").alias("department_count"))
*/
    val query = lines.
      writeStream.
      outputMode("update").
      format("console").
      trigger(Trigger.ProcessingTime("20 seconds")).
      start

    query.awaitTermination()

  }

}
