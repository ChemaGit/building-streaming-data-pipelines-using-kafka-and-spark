## Deep Dive into Producer and Consumer APIs – Scala
````text
- As we have gone through the basics of Producer and Consumer APIs, 
  now let us deep dive into Producer as well as Consumer APIs 
  so that we can start developing applications leveraging core features of Kafka.
    - Characteristics of Topic
    - Producer APIs – Partitioned Topic
    - Consumer APIs – Partitioned Topic
    - Consumer APIs – Managing Commits
    - Running as Fat Jar
    - Serialization and Deserialization
    - Admin APIs – Managing Topics
````

### Characteristics of Topic
````text
- Let us review Topic before we deep dive into APIs to produce messages as well as consume messages using Topic. 
  We shall also review scenarios which we have seen earlier.

- We should produce or consume messages in batches.
- By default message is sent immediately to the topic, however, we can batch the messages and it can improve performance significantly.
- The network will be used more efficiently and compression rates will be better when batched.

- Single Partition Topic
    - No two consumers from same consumer group can read from the same partition.
    - We can use multiple consumers belonging to different consumer groups to read the same copy of data from 
      the Topic or Topic partition multiple times using multiple threads.
    - With a single partition topic, our options are limited to improve the performance.

- Multi Partition Topic
    - Multi-Partition Topic gives us different options to improve performance significantly.
    - We can produce messages into partitions in round robin fashion. With CLI, 
      we will not be able to write messages into a specific partition or any custom algorithm. But APIs gives us a lot more flexibility.
    - We can consume messages from all partitions in round robin fashion or using group id or specifying the partition. 
    - However, with CLI, we will not be able to achieve other combinations such as some 
      partitions are consumed by one consumer while other partitions are consumed by another consumer in the same group. 
      APIs give us a lot more flexibility.
    - Configuring consumers as Consumer Group A as shown in the diagram is possible using APIs.
````

### Producer APIs – Partitioned Topic
````text
- Let us see some of the advanced options related to Producer APIs. 
  We will see how to produce messages to a partitioned topic, using batch, compression algorithms

- Let us start with deleting existing topic retail_multi and then recreating it with 4 partitions and replication factor as 1 in our local system.
- Here are the steps to send messages into Kafka Topic
    - Create Properties object with all relevant properties.
    - Create KafkaProducer object by passing Properties object.
    - Build ProducerRecord object using one of the constructors.
            - Topic Name and Value
            - Topic Name, Key and Value
            - Topic Name, Partition Index, Key and Value
            - and more with headers and timestamp.
    - Use KafkaProducer object’s send by passing ProducerRecord object.
    - Once the messages are sent make sure to close KafkaProducer.
    - Let us also review all the APIs that are available as part of ProducerRecord using :
        javap -p org.apache.kafka.clients.producer.ProducerRecord
````

### ProducerRecord with Value only
````text
- Let us see how we can use the simplest ProducerRecord using the constructor which takes topic name and value only.
    - We will be using access.log file that is being populated by gen_logs
    - Read data from access.log and create the collection out of it
    - Process collection using foreach, in which ProducerRecord object is created and then sent to Kafka Topic (retail_multi)
    - We can validate by running kafka-console-consumer.sh to consume messages from each partition and redirected 
      to file to understand the behavior of data distribution.
````
```bash
$ kafka-topics.sh \
  --zookeeper localhost:2181 \
  --delete \
  --topic retail_multi
  
$ rm -rf /tmp/kafka-logs

$ zookeeper-shell.sh localhost:2181 rmr /admin
$ zookeeper-shell.sh localhost:2181 rmr /config
$ zookeeper-shell.sh localhost:2181 rmr /brokers

$ kafka-server-start.sh -daemon /opt/kafka/config/server.properties

$ kafka-topics.sh \
  --zookeeper localhost:2181 \
  --create \
  --topic retail_multi \
  --partitions 4 \
--replication-factor 1

# start a consumer: 
$ kafka-console-consumer --bootstrap-server quickstart.cloudera:9092 --topic retail_multi --from-beginning
# run KafkaProducerFromFile
````
```scala
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

val props = new Properties
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

val producer = new KafkaProducer[Nothing, String](props)

import scala.io.Source

val logMessages = Source.
  fromFile("/opt/gen_logs/logs/access.log").
  getLines.
  toList

logMessages.foreach(message => {
  val record = new ProducerRecord("retail_multi", message)
  producer.send(record)
})
```

### ProducerRecord with Key

	- Now let us improvise and produce messages into Kafka Topic using key.

    	- When we pass the key to produce messages into the Partitioned topic, by default it will compute the hash of the key and then apply mod using the number of partitions used while creating Topic.
    	- It will ensure all the messages using the same key always go to the same partition.
    	- Let us go ahead and make necessary changes to grab ip address as key and then build ProducerRecord object to send it to Kafka Topic (retail_multi). 
	- Make sure retail_multi topic is cleaned up. On Windows, Kafka gets corrupted quite often for some reason, if that is the case you can execute below script to clean up and recreate retail_multi with 4 partitions.

````bash
$ kafka-topics.sh \
  --zookeeper localhost:2181 \
  --delete \
  --topic retail_multi
  
$ rm -rf /tmp/kafka-logs

$ zookeeper-shell.sh localhost:2181 rmr /admin
$ zookeeper-shell.sh localhost:2181 rmr /config
$ zookeeper-shell.sh localhost:2181 rmr /brokers

$ kafka-server-start.sh -daemon /opt/kafka/config/server.properties

$ kafka-topics.sh \
  --zookeeper localhost:2181 \
  --create \
  --topic retail_multi \
  --partitions 4 \
--replication-factor 1
````
````text
- Selection of key is subjective to your requirement. It can be dense (like country, region etc) or sparse like (ip address)
- We can validate by running kafka-console-consumer.sh to consume messages from each partition and 
  redirected to file to understand the behavior of data distribution. 
- Make sure to recreate retail_multi to validate successfully.
````
````bash
$ kafka-console-consumer --bootstrap-server quickstart.cloudera:9092 --topic retail_multi --from-beginning >> /home/cloudera/Documents/out.txt
- run ProducerFromFile	
````

```scala
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

val props = new Properties

props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

val producer = new KafkaProducer[String, String](props)

import scala.io.Source

val logMessages = Source.fromFile("/opt/gen_logs/logs/access.log").getLines.toList

logMessages.foreach(message => {
  val ipAddr = message.split(" ")(0)
  val record = new ProducerRecord("retail_multi", ipAddr, message)
  producer.send(record)
})
```

### ProducerRecord with Partition
````text
- We can also assign a particular partition based on custom logic while producing messages into Topic
    - This is primarily useful with dense keys such as country or region.
    - We can either map each unique key to the different partition or define custom logic to load balance the traffic.
    - In scenarios like one country or region generating abnormally high traffic than others, 
      we can have one or more partitions for that country or region and rest for other countries or regions.
    - We can also configure custom partitioner by using ProducerConfig.PARTITIONER_CLASS_CONFIG. 
      This approach is useful to define reusable custom partitioning strategy with in the application.
    - Let us first cleanup before developing the logic. 
- On Windows, Kafka gets corrupted quite often for some reason, 
  if that is the case you can execute below script to clean up and recreate retail_multi with 4 partitions.
````

```bash
$ kafka-topics.sh \
  --zookeeper localhost:2181 \
  --delete \
  --topic retail_multi
  
$ rm -rf /tmp/kafka-logs

$ zookeeper-shell.sh localhost:2181 rmr /admin
$ zookeeper-shell.sh localhost:2181 rmr /config
$ zookeeper-shell.sh localhost:2181 rmr /brokers

$ kafka-server-start.sh -daemon /opt/kafka/config/server.properties

$ kafka-topics.sh \
  --zookeeper localhost:2181 \
  --create \
  --topic retail_multi \
  --partitions 4 \
  --replication-factor 1
```
````text
- Now let us see an example. 
- As part of this program, we will extract ip address from each message and then get Country ISO code. 
- If it is US, we will send messages to partition 0 and for other countries, 
  we will send to the rest of the partitions using hash mod logic with partitions as 3 
  (which means data will for other Countries go into partition 1, 2, and 3). 
- Also if there are any invalid ips, we will send it to a different topic called retail_multi_invalid. 
  We will be using Java-based geoip2 provided by maxmind along with database with ip and country mapping.
````
```sbt
name := "KafkaWorkshop"
version := "1.0"
scalaVersion := "2.11.12"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "1.0.0"
libraryDependencies += "com.maxmind.geoip2" % "geoip2" % "2.12.0"
```


```scala
import java.util.Properties
import java.io.File
import com.maxmind.geoip2.DatabaseReader
import java.net.InetAddress
import scala.io.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

val props = new Properties()
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

val producer = new KafkaProducer[String, String](props)
val logMessages = Source.fromFile("/opt/gen_logs/logs/access.log").getLines.toList
val database = new File("/opt/maxmind/GeoLite2-Country.mmdb")
val reader = new DatabaseReader.Builder(database).build

logMessages.foreach(message => {
  try {
    val ipAddr = message.split(" ")(0)
    val countryIsoCode = reader.
      country(InetAddress.getByName(ipAddr)).
      getCountry.
      getIsoCode
    val partitionIndex = if (countryIsoCode == "US") 0
      else countryIsoCode.hashCode() % 3 + 1
    val record = new ProducerRecord[String, String]("retail_multi", partitionIndex, ipAddr, message)
    producer.send(record)
  } catch {
    case e: Exception => {
      val record = new ProducerRecord[String, String]("retail_multi_invalid", message)
      producer.send(record)
    }
  }
})
producer.close
```

### Using ProducerConfig
````text
- Let us see some of the additional properties from ProducerConfig that can be used for fine-tuning the performance of Producers.

- We can use additional properties of ProducerConfig to control batch size, compressing data etc.
- ProducerConfig.BATCH_SIZE_CONFIG can be used to control the batch size.
    - On the server on which program is running, data will be grouped based on the partition it need to send the data.
    - Producer will establish connection to the brokers who are leaders 
      for corresponding partition via bootstrap servers configured as part of the program.
    - When batch size is reached, corresponding data will be sent to the leader of each of the partition.
    - Leader will then write the first copy to the log file of the partition it is managing and will send the data to other followers as well.
- ProducerConfig.COMPRESSION_TYPE_CONFIG can be used to specify compression algorithm such as gzip, snappy, lz4 etc.
- There are settings to fine tune send buffer, receive buffer, buffer size for the batch etc.
````

### Build as Application
````text
- As we have explored Producer APIs with REPL, now it is time for us to develop applications.
- Here is the code which produces messages to a partitioned topic in round robin fashion. 
- We will validate by consuming each partition separately to see the behavior that 
  not all messages corresponding to the same key are stored in the same partition.
````

### build.sbt
```
name := "KafkaWorkshop"
version := "1.0"
scalaVersion := "2.11.12"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "1.0.0"
libraryDependencies += "com.maxmind.geoip2" % "geoip2" % "2.12.0"
```

### application.properties
```properties
dev.zookeeper = localhost:2181
dev.bootstrap.server = localhost:9092
prod.zookeeper = nn01.itversity.com:2181,nn02.itversity.com:2181,nn03.itversity.com:2181
prod.bootstrap.server = wn01.itversity.com:6667,wn02.itversity.com:6667
```

### ProduceLogMessagesFromFile.scala

```scala
import java.util.Properties
import scala.io.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object ProduceLogMessagesFromFile {

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getString("bootstrap.server"))
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)

    val inputDir = args(1)
    val topicName = args(2)

    val logMessages = Source.fromFile(inputDir).getLines.toList
    logMessages.foreach(message => {
      val record = new ProducerRecord[String, String](topicName, message)
      producer.send(record)
    })
    producer.close
  }
}
```
````text
- Here is the improvised code which produces messages to a partitioned topic using the key. 
- By default, it will apply hash on key (IP address) and then mod using the number of partitions. 
- We can validate by consuming each partition separately to see that all messages related to the same IP are in its corresponding partition.
````
```scala
import java.util.Properties
import scala.io.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object ProduceLogMessagesFromFileKey {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getString("bootstrap.server"))
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
   
    val producer = new KafkaProducer[String, String](props)

    val inputDir = args(1)
    val topicName = args(2)

    val logMessages = Source.fromFile(inputDir).getLines.toList
    logMessages.foreach(message => {
      //Use ip address as key
      val ipAddr = message.split(" ")(0)
      val record = new ProducerRecord[String, String](topicName, ipAddr, message)
      producer.send(record)
    })
    producer.close
  }
}
```
````text
- Here is the improvised code which produce messages to Kafka Topic as per custom logic. 
- This code uses geoip database and plugin to push US data to one partition and rest to other partitions. 
- Messages with invalid ips are also pushed to a different topic.
````

```scala
import java.util.Properties
import java.io.File
import com.maxmind.geoip2.DatabaseReader
import java.net.InetAddress
import scala.io.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

  
object ProduceLogMessagesFromFilePartition {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()
   
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getString("bootstrap.server"))
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val inputDir = args(1)
    val topicName = args(2)

    val logMessages = Source.fromFile(inputDir).getLines.toList

    val database = new File("src/main/resources/db/maxmind/GeoLite2-Country.mmdb")
    val reader = new DatabaseReader.Builder(database).build

    logMessages.foreach(message => {
      try {
        val ipAddr = message.split(" ")(0)
        val countryIsoCode = reader.country(InetAddress.getByName(ipAddr)).getCountry.getIsoCode
        val partitionIndex = if (countryIsoCode == "US") 2
                             else countryIsoCode.hashCode() % 2
        val record = new ProducerRecord[String, String](topicName, partitionIndex, ipAddr, message)
        producer.send(record)
      } catch {
        case e: Exception => {
          val record = new ProducerRecord[String, String](topicName + "_invalid", message)
          producer.send(record)
        }
      }
    })
    producer.close
  }
  
}
```
````text
- We can also pass timestamp as well as partition index while building ProducerRecord. 
  However, we will leave it to you as an exercise to explore and see the behavior.
````

### Consumer APIs – Partitioned Topic
````text
- As we have understood APIs associated with Producer in detail now it is time for us to explore Consumer APIs in detail.

- Here are the steps to consume messages from Kafka Topic 
        - Create Properties object with all relevant properties.
        - Create KafkaConsumer object by passing Properties object.
        - Subscribe to Topic or Assign to Topic Partition
        - Poll in infinite loop
        - Poll will return ConsumerRecords object. It is collection of ConsumerRecord objects.
        - In a for loop we can iterate through ConsumerRecords and perform necessary action.

- consumer.poll will take the duration of a poll as argument and return ConsumerRecords
- Let us also review all the APIs that are available as part of ConsumerRecord using :javap -p ConsumerRecord
- Multiple consumer groups are used to process the same data for different purposes.
````

### Subscribing to Topic
````text
- As we have understood important APIs to consume messages from Kafka Topic, let us create an application using IDE and understand different partition assignment strategies.
    - If you subscribe one consumer to a Multi Partitioned Topic, data will be read from all partitions in round robin fashion
    - As the data is consumed, offset will be committed. In the subsequent runs using same consumer group with offset being earliest, data will be consumed from the last offset not earliest.
    - We can use seek to reset the offset, but it is not effective with subscribe against the partitioned topic.
    - If you subscribe multiple consumers to a Multi Partitioned Topic, by default range of the subset of partitions will be processed by each consumer.
    - We can also specify round robin to change the partition assignment strategy using ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG or partition.assignment.strategy 
    - (e.g.: props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, “org.apache.kafka.clients.consumer.RoundRobinAssignor”))
    - We can implement custom partition assignment strategy and use it by specifying the fully qualified class name as part of the above-mentioned property.
    - Here is the example of a consumer subscribing to a Topic. We will open two sessions and run two separate jobs simultaneously to understand the behavior.
````

```scala
import java.util.{Properties, Collections}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import scala.collection.JavaConversions._

object ConsumeLogMessagesFromTopicSubscribe {
  def main(args: Array[String]): Unit = { 
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,envProps.getString("bootstrap.server"))
    props.put(ConsumerConfig.CLIENT_ID_CONFIG,"Consume Messages from Topic")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "dg30")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    
    // props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,"range")
    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(Collections.singletonList(args(1)))

    while(true) {
      val records = consumer.poll(500)
      for(record <- records) {
        println("Received message: (" + record.key()
          + ", " + record.value()
          + ") from " + record.partition()
          + " at offset " + record.offset())
      }
    }
  }
}
```

### Assigning Topic Partition
````text
- Let us understand how to assign a particular Topic Partition to a Consumer Group.
    - We can also assign specific partitions to each of the consumers in a consumer group using assign. 
    - It takes the collection of TopicPartition as an argument. 
    - We can build TopicPartition object using topic name along with the partition index.
    - Here is the example where a partition is assigned to a Consumer Group.
````

```scala
import java.util.{Collections, Properties}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import scala.collection.JavaConversions._

object ConsumeLogMessagesFromTopic {

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,envProps.getString("bootstrap.server"))
    props.put(ConsumerConfig.CLIENT_ID_CONFIG,"Consume messages from topic")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer")

    props.put(ConsumerConfig.GROUP_ID_CONFIG, "cgb3")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

    val consumer = new KafkaConsumer[String, String](props)
    val partitions = Set(new TopicPartition(args(1), args(2).toInt))
    consumer.assign(partitions)
    consumer.seekToBeginning(consumer.assignment())

    while(true) {
      val records = consumer.poll(500)
      for(record <- records) {
        println("Received message: (" + record.key()
          + ", " + record.value()
          + ") from " + record.partition()
          + " at offset " + record.offset())
      }
      Thread.sleep(100)
    }
  }
}
```
````text
- We can also implement an assignment strategy of assigning 
  all even partitions to one consumer while odd partitions to other. 
- However, we are leaving it as an exercise for you.
````

### Consumer APIs – Managing Commits
````text
- Now let us see how we can perform commit as part of consumers.
    - Commit is nothing but saving offset periodically. The offset will be committed to internal topics managed by Kafka.
    - If you look at message structure, for each message being consumed include topic name, partition name, offset and other information.
    - If auto-commit is enabled, offset of the last message returned by poll will be committed after processing all the messages.
    - If you poll every 5 seconds, then commit will happen every 5 seconds
    - This can cause duplicates while reprocessing of data if there are any failures as part of the batch. Reprocessing is also called as rebalancing.
    - However, based upon the nature of data we might want to commit as per pre-defined logic manually.
    - With manual commit, Kafka supports commitSync as well as commitAsync.
    - If your consumer needs to commit very often, then the overhead of committing using commitSync is considerable as the consumer need to wait until commit is complete to process new data.
    - commitAsync will just submit commit request and proceed to process next batch of data.
    - We can pass the current offset to commitSync as well as commitAsync as part of the manual commit process. 
````   	