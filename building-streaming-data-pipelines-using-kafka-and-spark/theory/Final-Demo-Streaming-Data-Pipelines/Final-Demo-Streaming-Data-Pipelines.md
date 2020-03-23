## Final Demo – Streaming Data Pipelines
````text
- As we understood all the moving parts such as logstash, kafka, spark streaming, hbase etc in building streaming data pipelines, let us see how we can integrate all these things together and come up with solution.
- Steps:
- Run Genlogs Script to generate retail e-commerce platform logs
    - Use Logstash to transform logs into JSON format and also perform some data transformations for ease of use.
    - Use Spark Streaming to fetch data from kafka and use map reduce to get the country count.
    - Update HBase Counts
````

### Logstash Configuration:
````text
- We will be using logstash to parse the gen_logs file and perform the following transformations:
        - Parse the apache combined log format and convert it to JSON
        - Convert Response, Bytes and Response time to integers and float.
        - Add a parameter rounded timestamp rounded to the nearest minute
        - Use Geoip plugin to add country details from IP Address
    - Let's edit logstash.config
````

```editorconfig
input {
  file {
    path => ["/opt/gen_logs/logs/access.log"]
    type => "apache_access"
  }
}
filter {
    grok {
       match => [
       "message" , "%{COMBINEDAPACHELOG}+%{GREEDYDATA:extra_fields}",
       "message" , "%{COMMONAPACHELOG}+%{GREEDYDATA:extra_fields}"
       ]
       overwrite => [ "message" ]
    }
    mutate {
       convert => ["response", "integer"]
       convert => ["bytes", "integer"]
       convert => ["responsetime", "float"]
       add_field => [ "rounded_timestamp", "%{@timestamp}" ]
       convert => ["rounded_timestamp", "string"]
    }
    geoip {
       source => "clientip"
       target => "geoip"
       add_tag => [ "apache-geoip" ]
    }
    ruby {
      code => "
       timestamp_stripped = event.get('rounded_timestamp')[0..16] + '00.000Z'
       event.set('rounded_timestamp',timestamp_stripped)
      "
    }
    useragent {
       source => "agent"
    }
}
output {
  kafka {
    codec => json
    topic_id => "retail_logs"
    bootstrap_servers => quickstart.cloudera:9092"
  }
}
```
````bash
# Command to run logstash agent: 
$ sudo /usr/share/logstash/bin/logstash -f logstash.config
$ start_logs
$ kafka-console-consumer --bootstrap-server quickstart.cloudera:9092 --topic retail_logs --from-beginning
````

### Project Dependencies (build.sbt)
````text
- build.sbt file contains the dependencies which we require for the project.
- Spark, Spark Streaming and Spark Streaming Kafka Connector jars for Spark Dependencies
- Kafka and Kafka Clients jar as Kafka Dependencies
- HBase Client & Common along with Hadoop Common jars are required to connect and write data to HBase.
- Typesafe config jar to externalize the properties
````

```buildoutcfg
name := "StreamingPipelinesDemo"
version := "0.1"
scalaVersion := "2.11.11"
libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.3.0"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.3.0"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.3.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.3.0"
libraryDependencies += "org.apache.hbase" % "hbase-client" % "1.1.8"
libraryDependencies += "org.apache.hbase" % "hbase-common" % "1.1.8"
libraryDependencies += "org.apache.kafka" %% "kafka" % "1.1.0"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "1.1.0"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.0"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
```

### Application Properties (application.properties)
````text
- We have defined the following properties for the application:
- Zookeeper URLs
    - Zookeeper Quorum URL
    - Zookeeper Port
    - Kafka Bootstrap Servers
    - Streaming Window
````
```properties
dev.zookeeper = localhost:2181
dev.bootstrap.server = localhost:9092
dev.zookeeper.quorum = localhost
dev.zookeeper.port = 2181
dev.window = 20
prod.zookeeper = nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181
prod.zookeeper.quorum = nn01.itversity.com,nn02.itversity.com,rm01.itversity.com
prod.zookeeper.port = 2181
prod.bootstrap.server = nn01.itversity.com:6667,nn02.itversity.com:6667,rm01.itversity.com:6667
prod.window = 20
```

### Spark Streaming Code to get Visit Count by Country

```scala
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
    //Insert Into Tableimport org.apache.spark.streaming.kafka010.
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
    val sparkConf = new SparkConf().setMaster("yarn").setAppName("SiteTraffic")
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
```

### Build, Deploy and Run
````text
- Right click on the project and copy path
- Go to terminal and run cd command with the path copied
- Run sbt package
- It will generate jar file for our application
- Copy to the server where you want to deploy
- Run below command in another session on the server
````

```bash
$ /opt/mapr/spark/spark-2.2.1/bin/spark-submit \
--class CountryVisitCount \
--master yarn  \
--conf spark.ui.port=4926  \
--jars $(echo /external_jars/*.jar | tr ' ' ',') \
kafkaworkshopmapr_2.11-0.1.jar prod

# Command to run logstash agent: 
# sudo /usr/share/logstash/bin/logstash -f /home/cloudera/files/logstash.config
$ start_logs
$ kafka-console-consumer --bootstrap-server quickstart.cloudera:9092 --topic retail_logs --from-beginning
````