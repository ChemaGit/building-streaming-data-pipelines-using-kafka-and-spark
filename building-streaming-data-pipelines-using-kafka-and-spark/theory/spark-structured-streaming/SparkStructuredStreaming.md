## Overview of Streaming Technologies and Spark Structured Streaming
````text
- As part of this session we will see the overview of technologies used in building Streaming data pipelines. 
- Also we will have deeper look into Spark Structured Streaming by developing solution for a simple problem.

- Overview of Streaming Technologies
    - Ingestion
    - Processing in real time
    - Databases
    - Visualization
    - Frameworks for building streaming pipelines

- Spark Structured Streaming
    - Basic Concepts
    - Read log messages in streaming fashion
    - Process and run real time analytics
    - Handling late data and watermarking
````

### Ingestion
````text
- Data Integration Batch or Real Time
    - For batch get data from databases by querying data from Database
    - Batch Tools: Informatica, Ab Initio, etc
    - For real time get data from web server logs or databases logs
    - Real time tools: Goldengate to get data from database logs, Kafka to get data from web server logs

- There are many technologies which are used in ingesting data in real time.
    - Logstash
            - Can read data from different sources
            - Can apply simple row level transformations such as converting date formats, masking some of the attribute values in each message etc.
            - Can work with other technologies in building streaming pipelines such as Kafka
- Flume
        - Runs as agent
        - Each agent have source, channel and sink
        - Supports many sources and sinks
        - Can work with other technologies in building streaming pipelines such as kafka
        - Can push data to technologies like Storm, Flink, Spark Streaming etc to run real time streaming analytics.
- Kafka connect and Kafka topic
        - Kafka topic is false tolerant and highly reliable intermediate data streaming mechanism
        - Kafka connect is to read data from different sources and push messages to Kafka topic and also consume messages from Kafka topic and push to supported targets.
        - Kafka connect and topic will facilitate use to get data from different types of sources to different types of sinks.
        - Can push data to technologies like Kafka Streams, Storm, Flink, Spark Streaming etc to run real time streaming analytics.
- Kinesis firehose and Kinesis data streams
        - Kinesis is AWS Service which is very similar to Kafka
        - Kinesis Firehose is similar to Kafka connect and Kinesis data streams is similar to topic
        - No need of dedicated cluster and will only be charged for the usage.
- and more
````

### Real time processing
````text
- As the data come through tools like logstash, flume, kafka etc we might want to perform standard transformations such as data cleansing, standardization, lookups, joins, aggregations, sorting, ranking etc. 
- While some of the data ingestion tools are capable of some of the transformations they do not come up with all the features. 
- Also the ingestion might get delayed and make the flow unstable. 
- Hence we need to use the tools which are built for performing transformations as the data is streamed. Here are some of the prominent tools.
        - Spark Streaming or Spark Structured Streaming (a module built as part of Spark)
        - Flink
        - Storm
        - Kafka Streams
        - and more
````

### Databases
````text
- Once the data is processed, we have to store data in databases to persist and build visualization layer on top of the processed data. We can use
        - RDBMS – such as Oracle, MySQL, Postgres etc
        - Data Warehouses – such as Teradata, Redshift etc
        - NoSQL databases – such as HBase, Cassandra, MongoDB, DynamoDB etc
        - Search based databases – such as Elastic Search
````

### Visualization
````text
- Visualization is typically done as part of the application development using standard frameworks.
    - d3js
    - Kibana
    - Standard reporting tools such as Tableau
    - and more
````

### Frameworks
````text
- As we discuss about different moving parts in building streaming pipelines now let us get into frameworks. 
  Most of these frameworks do not have visualization included.
- Kafka
    - Kafka Connect
    - Kafka Topic
    - Kafka Streams
- ELK
    - Elastic Search (Database)
    - Logstash (streaming and processing logs)
    - Kibana (Visualization)
- HDF – Streaming services running behind NiFi
- MapR Streams – Streaming services running on MapR cluster
- AWS Services
    - DynamoDB (Database)
    - s3 (persistent storage of flat file format)
    - Kinesis (streaming and processing logs)

- We have highlighted some of the popular frameworks. 
- Almost all the top vendors such as Cloudera, Google, Microsoft Azure etc have necessary services to build streaming pipelines.
````

### Spark Structured Streaming
````text
- Apache Spark is a proven distributed computing framework with modules for different purposes
    - Core APIs – Transformations and Actions
    - Spark SQL and Data Frames
    - Spark Streaming (legacy) and Spark Structured Streaming
    - Spark MLLib
    - Spark GraphX
    - and more

- We can use Spark Streaming to apply complex business rules either by using Data Frame operations or Spark SQL. 
- Let us see a demo about getting started with Spark Structured Streaming. Main demo is done with legacy streaming.
````

### Important Concepts
````text
- Sources
    - File
    - Kafka
    - Socket (for testing)
- Basic Operations
- Window Operations on Event Time
    - Handling late data and watermarking
- Output Modes
    - Append Mode
    - Update Mode
    - Complete Mode
- Sinks/Targets
    - Console
    - File
    - Memory
    - Kafka
    - foreach (can be used to write to Database)
````

### Development Life Cycle
````text
- Here are the steps involved to get started with HBase
    - Make sure gen_logs is setup and data is streamed being streamed
- Create new project StreamingDemo using IntelliJ
    - Choose scala 2.11
    - Choose sbt 0.13.x
    - Make sure JDK is chosen
- Update build.sbt. See below
- Define application properties
- Create GetStreamingDepartmentTraffic object
- Add logic to process data using Spark Structured Streaming
- Build jar file
- Ship to cluster and deploy
````

### Dependencies (build.sbt)
````text
- Spark structured streaming require Spark SQL dependencies.
    - Add type safe config dependency so that we can externalize properties
        - Add spark-core and spark-sql dependencies
        - Replace build.sbt with below lines of code
````
````sbt
name := "StreamingDemo"
version := "1.0"
scalaVersion := "2.11.12"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.0"
````

### Externalize Properties
````text
- We need to make sure that application can be run in different environments. 
- It is very important to understand how to externalize properties and pass the information at run time.
        - Make sure build.sbt have dependency related to type safe config
        - Create new directory under src/main by name resources
        - Add file called application.properties and add below entries
````
````properties
dev.execution.mode = local
dev.data.host = quickstart.cloudera
dev.data.port = 9999
prod.execution.mode = yarn
//make sure you use appropriate host for which you have access to
prod.data.host = gw02.itversity.com
prod.data.port = 9999
````

### Create GetStreamingDepartmentTraffic Program
````text
- Create scala program by choosing Scala Class and then type Object
- Make sure program is named as GetStreamingDepartmentTraffic
- First we need to import necessary APIs
- Develop necessary logic
    - Get the properties from application.properties
    - Create spark session object by name spark
    - Create stream using spark.readStream
    - Process data using Data Frame Operations
    - Write the output to console (in actual applications we write the output to database)
````

````scala
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

    val departmentLines = lines.
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

    val query = departmentTraffic.
      writeStream.
      outputMode("update").
      format("console").
      trigger(Trigger.ProcessingTime("20 seconds")).
      start

    query.awaitTermination()
  }
}	
// $ tail_logs | nc -lk 9999
// run the application GetStreamingDepartmentTraffic 
````

### Build, Deploy and Run
````bash
$ tail_logs | nc -lk quickstart.cloudera 44444	
# on sbt
$ sbt
sbt> show discoveredMainClasses
sbt> runMain structuredstreamingdemo.GetStreamingDepartmentTraffic dev
````
````text
- Deploy and run on the server
- Right click on the project and copy path
    - Go to terminal and run cd command with the path copied
    - Run sbt package
    - It will generate jar file for our application
    - Copy to the server where you want to deploy
    - Start streaming tail_logs to web service – tail_logs.sh|nc -lk gw02.itversity.com 9999
    - Run below command in another session on the server
````
````
````bash
$ tail_logs | nc -lk quickstart.cloudera 44444

$ spark2-submit \
  --master yarn \
  --class structuredstreamingdemo.GetStreamingDepartmentTraffic \
  --conf spark.ui.port=12901 \
  --jars "/home/cloudera/.ivy2/cache/com.typesafe/config/bundles/config-1.3.2.jar" \
target/scala-2.11/building-streaming-data-pipelines-using-kafka-and-spark_2.11-0.1.jar dev


$ spark2-submit \
  --master yarn \
  --class structuredstreamingdemo.GetStreamingDepartmentTraffic \
  --conf spark.ui.port=12901 \
  --packages com.typesafe:config:1.3.2 \
target/scala-2.11/building-streaming-data-pipelines-using-kafka-and-spark_2.11-0.1.jar dev
````

### Kafka and Spark Structured Streaming – Integration
````text
- Let us see how we can get data from Kafka topic and process using Spark Structured Streaming.
````


### Development Life Cycle
````text
- Here are the steps involved to get started with HBase
    - Make sure gen_logs is setup and data is being streamed
    - Create new project StreamingDemo using IntelliJ
        - Choose scala 2.11
        - Choose sbt 0.13.x
        - Make sure JDK is chosen
    - Update build.sbt. See below
    - Define application properties
    - Create GetStreamingDepartmentTraffic object
    - Add logic to process data using Spark Structured Streaming
    - Build jar file
    - Ship to cluster and deploy
````

### Dependencies (build.sbt)
````text
- Spark structured streaming require Spark SQL dependencies.
    - Add type safe config dependency so that we can externalize properties
    - Add spark-sql dependencies
    - Replace build.sbt with below lines of code
````
````sbt
name := "KafkaWorkshop"
version := "1.0"
scalaVersion := "2.11.12"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "1.0.0"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.2.0"
libraryDependencies += "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % "2.2.0"
````

### Externalize Properties
````text
- We need to make sure that application can be run in different environments. 
- It is very important to understand how to externalize properties and pass the information at run time.
    - Make sure build.sbt have dependency related to type safe config
    - Create new directory under src/main by name resources
    - Add file called application.properties and add below entries
    - It should include information related to Kafka broker
````

````properties
dev.execution.mode = local
dev.data.host = localhost
dev.data.port = 9999
dev.bootstrap.servers = localhost:9092
prod.execution.mode = yarn
prod.data.host = gw02.itversity.com
prod.data.port = 9999
prod.bootstrap.servers = wn01.itversity.com:6667,wn02.itversity.com:6667
````

### Create GetStreamingDepartmentTraffic Program
````text
- Create scala program by choosing Scala Class and then type Object
- Make sure program is named as GetStreamingDepartmentTraffic
- First we need to import necessary APIs
- Develop necessary logic
    - Get the properties from application.properties
    - Create spark session object by name spark
    - Create stream using spark.readStream
    - Pass broker information and topic information as options
    - Process data using Data Frame Operations
    - Write the output to console (in actual applications we write the output to database)
````
````scala
import java.sql.Timestamp
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
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

    val lines = spark.readStream.
      format("kafka").
      option("kafka.bootstrap.servers", conf.getString("bootstrap.servers")).
      option("subscribe", "retail_multi").
      option("includeTimestamp", true).
      load.
      selectExpr("CAST(value AS STRING)", "timestamp").
      as[(String, Timestamp)]

    val departmentTraffic = lines.
      where(split(split($"value", " ")(6), "/")(1) === "department").
      select(split(split($"value", " ")(6), "/")(2).alias("department_name"), $"timestamp").
      groupBy(
        window($"timestamp", "20 seconds", "20 seconds"),$"department_name"
      ).
      count()

    val query = departmentTraffic.
      writeStream.
      outputMode("update").
      format("console").
      trigger(Trigger.ProcessingTime("20 seconds")).
      start()

    query.awaitTermination()
  }
}
````
````bash
spark2-submit \
  --class structuredstreamingintegration.GetStreamingDepartmentTrafficKafka \
  --master yarn \
  --conf spark.ui.port=12901 \
  --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 \
bcstructuredstreamingdemo-assembly-1.0.jar prod update
````

### Validate locally
````bash
$ Make sure zookeeper and Kafka broker are running
$ start_logs
# Start streaming tail_logs to Kafka Broker 
$ tail_logs.sh | kafka-console-producer.sh --broker-list quickstart.cloudera:9092 --topic retail
# Run the program using IDE to make sure we see output in the console

# From sbt
$ start_logs
$ tail_logs | kafka-console-producer --broker-list quickstart.cloudera:9092 --topic retail_multi
$ sbt
sbt> compile
sbt> runMain runMain structuredstreamingintegration.GetStreamingDepartmentTrafficKafka dev
````

### Build, Deploy and Run
````text
- Right click on the project and copy path
- Go to terminal and run cd command with the path copied
- Run sbt package
- It will generate jar file for our application
- Copy to the server where you want to deploy
- Make sure zookeeper and Kafka Broker are running
- $ start_logs
- Start streaming tail_logs to web service – tail_logs.sh|kafka-console-producer.sh --broker-list nn01.itversity.com:6667,nn02.itversity.com:6667,rm01.itversity.com:6667 --topic retail
- Run below command in another session on the server
````
````bash
$ sbt package
$ export SPARK_KAFKA_VERSION=0.10
$ spark2-submit \
    --master local \
    --class structuredstreamingintegration.GetStreamingDepartmentTrafficKafka \
    --conf spark.ui.port=12901 \
    --jars "/home/cloudera/.ivy2/cache/com.typesafe/config/bundles/config-1.3.2.jar,/home/cloudera/.ivy2/cache/org.apache.spark/spark-sql_2.11/jars/spark-sql_2.11-2.2.0.jar,/home/cloudera/.ivy2/cache/org.apache.kafka/kafka-clients/jars/kafka-clients-1.0.0.jar,/home/cloudera/.ivy2/cache/org.apache.spark/spark-core_2.11/jars/spark-core_2.11-2.2.0.jar" \
    --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.2.0 \
      target/scala-2.11/building-streaming-data-pipelines-using-kafka-and-spark_2.11-0.1.jar dev
````    
