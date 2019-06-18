package finaldemostreamingdatapipelines

import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Get, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.KafkaUtils

import scala.util.parsing.json.JSON

object StreamingPipelinesDemo {

  // HBase Connectors and Update Function:

  def getHbaseConnection(config: Config): Connection ={
    //Create Hbase Configuration Object
    val hBaseConf: Configuration = HBaseConfiguration.create()
    hBaseConf.set("hbase.zookeeper.quorum", config.getString("zookeeper.quorum"))
    hBaseConf.set("hbase.zookeeper.property.clientPort", config.getString("zookeeper.port"))
    hBaseConf.set("zookeeper.znode.parent","/hbase-unsecure")
    hBaseConf.set("hbase.cluster.distributed","true")
    //Establish Connection
    val connection = ConnectionFactory.createConnection(hBaseConf)
    connection
  }

  def insertOrUpdateMetrics(rowId: String, country: String, count: Int , envProps: Config): Unit = {
    //Hbase Metadata
    val columnFamily1 = "metrics"
    val connection = getHbaseConnection(envProps)

    val table = connection.getTable(TableName.valueOf("/user/mapr/country_count_stream"))
    val row_get = new Get(Bytes.toBytes(rowId.toString))
    //Insert Into Table
    val result = table.get(row_get)
    val value = result.getValue(Bytes.toBytes(columnFamily1),Bytes.toBytes(country))

    val rowPut = new Put(Bytes.toBytes(rowId.toString))
    if (value == null) {
      rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes(country),Bytes.toBytes(count.toString))
    } else {
      val newCount = Bytes.toString(value).toInt + count
      rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes(country),Bytes.toBytes(newCount.toString))
    }
    table.put(rowPut)
    connection.close()
  }


  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps: Config = conf.getConfig(args(0))
    val sparkConf = new SparkConf().setMaster("local").setAppName("SiteTraffic")
    val streamingContext = new StreamingContext(sparkConf, Seconds(envProps.getInt("window")))
    val broadcastConfig = streamingContext.sparkContext.broadcast(envProps)
    val topicsSet = Set("retail_logs")
    val now = Calendar.getInstance().getTime()
    val timestamp = streamingContext.sparkContext.broadcast(now)
    val kafkaParams = Map[String, Object](

      "bootstrap.servers" -> envProps.getString("bootstrap.server"),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "1",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val logData: DStream[String] =KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topicsSet, kafkaParams)
    ).map(record => record.value)

    val countryList = logData.map(line => {
      val json: Option[Any] = JSON.parseFull(line)
      val map = json.get.asInstanceOf[Map[String, Any]]
      val geoIpMap = map.get("geoip").get.asInstanceOf[Map[String, Any]]
      val country = geoIpMap.get("country_name").getOrElse("ALIEN").asInstanceOf[String]
      val timestamp = map.get("rounded_timestamp").get.asInstanceOf[String]
      ((timestamp , country), 1)
    }).reduceByKey(_ + _)

    countryList.foreachRDD(countries =>{
      countries.foreach(country =>{
        insertOrUpdateMetrics(country._1._1, country._1._2, country._2, broadcastConfig.value)
      })
    })

    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
