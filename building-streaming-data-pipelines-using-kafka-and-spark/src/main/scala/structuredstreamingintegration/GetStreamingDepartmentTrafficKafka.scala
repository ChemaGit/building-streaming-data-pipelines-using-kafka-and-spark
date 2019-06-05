package structuredstreamingintegration

import java.sql.Timestamp

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

// Created by itversity on 19/05/18.

object GetStreamingDepartmentTrafficKafka {

  def main(args: Array[String]): Unit = {

    val conf = ConfigFactory.load.getConfig(args(0))
    val spark = SparkSession.
      builder().
      master(conf.getString("execution.mode")).
      appName("Get Streaming Department Traffic").
      getOrCreate()

    import spark.implicits._
    spark.sparkContext.setLogLevel("ERROR")
    spark.conf.set("spark.sql.shuffle.partitions", "2")

    val lines: Dataset[(String, Timestamp)] = spark.readStream.
      format("kafka").
      option("kafka.bootstrap.servers", conf.getString("bootstrap.servers")).
      option("subscribe", "retail_multi").
      option("includeTimestamp", true).
      load.
      selectExpr("CAST(value AS STRING)", "timestamp").
      as[(String, Timestamp)]

    val departmentTraffic : DataFrame = lines.
      where(split(split($"value", " ")(6), "/")(1) === "department").
      select(split(split($"value", " ")(6), "/")(2).alias("department_name"), $"timestamp").
      groupBy(
        window($"timestamp", "20 seconds", "20 seconds"),$"department_name"
      ).count().toDF("window","department_name","count")

    val query = departmentTraffic.
      writeStream.
      outputMode("update").
      format("console").
      trigger(Trigger.ProcessingTime("20 seconds")).
      start()

    query.awaitTermination()
  }

}
