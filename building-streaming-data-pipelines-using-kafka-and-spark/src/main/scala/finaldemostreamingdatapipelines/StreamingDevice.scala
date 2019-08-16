package finaldemostreamingdatapipelines

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Get, HBaseAdmin, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.KafkaUtils
import java.util.Date
import java.text.SimpleDateFormat

import scala.concurrent.duration.Duration
import scala.util.parsing.json.JSON

object StreamingDevice {

  def getHbaseConnection(config: Config): Connection = {
    //Create Hbase Configuration Object
    val hBaseConf: Configuration = HBaseConfiguration.create()
    hBaseConf.set("hbase.zookeeper.quorum", config.getString("zookeeper.quorum"))
    //hBaseConf.set("hbase.zookeeper.property.clientPort", config.getString("zookeeper.port"))
    //hBaseConf.set("zookeeper.znode.parent","/hbase-unsecure")
    //hBaseConf.set("hbase.cluster.distributed","true")
    //Establish Connection
    val connection = ConnectionFactory.createConnection(hBaseConf)
    connection
  }

  def insertOrUpdateMetrics(rowId: String, deviceId: String, temperature: String ,longitude: String,latitude: String,time: String, envProps: Config): Unit = {
    //Hbase Metadata
    val columnFamily1 = "data"
    val connection = getHbaseConnection(envProps)

    val admin = connection.getAdmin
    if (admin.isTableAvailable(TableName.valueOf(Bytes.toBytes("device_iot:device")))) {
      val table = connection.getTable(TableName.valueOf("device_iot:device"))

      val row_get = new Get(Bytes.toBytes(rowId))
      //Insert Into Table
      val result = table.get(row_get)
      val value = result.getValue(Bytes.toBytes(columnFamily1),Bytes.toBytes(deviceId))
      if(value == null) {
        val rowPut = new Put(Bytes.toBytes(rowId.toString))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("deviceID"),Bytes.toBytes(deviceId))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("temperature"),Bytes.toBytes(temperature))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("latitude"),Bytes.toBytes(latitude))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("longitude"),Bytes.toBytes(longitude))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("time"),Bytes.toBytes(time))
        table.put(rowPut)
      }
    }else {
      println("Table is not available, I don't know why")
    }
    connection.close()
  }

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps: Config = conf.getConfig(args(0))
    val sparkConf = new SparkConf().setMaster(envProps.getString("execution.mode")).setAppName("Device Signal")
    val streamingContext = new StreamingContext(sparkConf, Seconds(envProps.getInt("window")))
    streamingContext.sparkContext.setLogLevel("ERROR")
    val broadcastConfig = streamingContext.sparkContext.broadcast(envProps)
    val topicsSet = Set(envProps.getString("topic"))
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> envProps.getString("bootstrap.server"),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "1",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val logData: DStream[String] = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topicsSet, kafkaParams)
    ).map(record =>{
      record.value
    })

    val countryList = logData.map(line => {
      val json: Option[Any] = JSON.parseFull(line)
      val map = json.get.asInstanceOf[Map[String, Any]]
      val data = map.get("data").getOrElse("Weird").asInstanceOf[Map[String, Any]]
      val deviceId = data.get("deviceId").getOrElse("Weird").asInstanceOf[String]
      val temperature = data.get("temperature").getOrElse("Weird").asInstanceOf[Double]
      val time = data.get("time").getOrElse("Weird").asInstanceOf[String]
      val location = data.get("location").getOrElse("Weird").asInstanceOf[Map[String,String]]
      val latitude = location.get("latitude").getOrElse("Weird")
      val longitude = location.get("longitude").getOrElse("Weird")
      val date = new Date(time.toLong)
      val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(date)
      (s"$deviceId-$time",deviceId,temperature.toInt.toString, longitude, latitude,format)
    })


    countryList.foreachRDD(devices =>{
      devices.foreach(device =>{
        insertOrUpdateMetrics(device._1,device._2,device._3,device._4,device._5,device._6, broadcastConfig.value)
      })
    })

    streamingContext.start()
    streamingContext.awaitTermination()
  }

}
