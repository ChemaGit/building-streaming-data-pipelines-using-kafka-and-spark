# Deep Dive into Producer and Consumer APIs – Scala

	- As we have gone through the basics of Producer and Consumer APIs, now let us deep dive into Producer as well as Consumer APIs so that we can start developing applications leveraging core features of Kafka.

    		- Characteristics of Topic
    		- Producer APIs – Partitioned Topic
    		- Consumer APIs – Partitioned Topic
    		- Consumer APIs – Managing Commits
    		- Running as Fat Jar
    		- Serialization and Deserialization
    		- Admin APIs – Managing Topics


# Characteristics of Topic

	- Let us review Topic before we deep dive into APIs to produce messages as well as consume messages using Topic. We shall also review scenarios which we have seen earlier.

	- We should produce or consume messages in batches.
	- By default message is sent immediately to the topic, however, we can batch the messages and it can improve performance significantly.
	- The network will be used more efficiently and compression rates will be better when batched.

	- Single Partition Topic
    		- No two consumers from same consumer group can read from the same partition.
    		- We can use multiple consumers belonging to different consumer groups to read the same copy of data from the Topic or Topic partition multiple times using multiple threads.
    		- With a single partition topic, our options are limited to improve the performance.

	- Multi Partition Topic
    		- Multi-Partition Topic gives us different options to improve performance significantly.
    		- We can produce messages into partitions in round robin fashion. With CLI, we will not be able to write messages into a specific partition or any custom algorithm. But APIs gives us a lot more flexibility.
    		- We can consume messages from all partitions in round robin fashion or using group id or specifying the partition. 
		- However, with CLI, we will not be able to achieve other combinations such as some partitions are consumed by one consumer while other partitions are consumed by another consumer in the same group. APIs give us a lot more flexibility.
    		- Configuring consumers as Consumer Group A as shown in the diagram is possible using APIs.


# Producer APIs – Partitioned Topic

	- Let us see some of the advanced options related to Producer APIs. We will see how to produce messages to a partitioned topic, using batch, compression algorithms

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
    		- Let us also review all the APIs that are available as part of ProducerRecord using :javap -p org.apache.kafka.clients.producer.ProducerRecord


# ProducerRecord with Value only

	- Let us see how we can use the simplest ProducerRecord using the constructor which takes topic name and value only.

    		- We will be using access.log file that is being populated by gen_logs
    		- Read data from access.log and create the collection out of it
    		- Process collection using foreach, in which ProducerRecord object is created and then sent to Kafka Topic (retail_multi)
    		- We can validate by running kafka-console-consumer.sh to consume messages from each partition and redirected to file to understand the behavior of data distribution.

kafka-topics.sh \
  --zookeeper localhost:2181 \
  --delete \
  --topic retail_multi
  
rm -rf /tmp/kafka-logs

zookeeper-shell.sh localhost:2181 rmr /admin
zookeeper-shell.sh localhost:2181 rmr /config
zookeeper-shell.sh localhost:2181 rmr /brokers

kafka-server-start.sh -daemon /opt/kafka/config/server.properties

kafka-topics.sh \
  --zookeeper localhost:2181 \
  --create \
  --topic retail_multi \
  --partitions 4 \
--replication-factor 1

	- start a consumer: kafka-console-consumer --bootstrap-server quickstart.cloudera:9092 --topic retail_multi --from-beginning
	- run KafkaProducerFromFile

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


# Now let us improvise and produce messages into Kafka Topic using key.