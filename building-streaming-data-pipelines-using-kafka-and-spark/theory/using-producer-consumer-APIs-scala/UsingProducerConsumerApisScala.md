# Using Producer and Consumer APIs – Scala

	- As part of this topic we will see how we can develop programs to produce messages to Kafka Topic and consume messages from Kafka Topic using Scala as Programming language.
    		- Revision about Kafka Topic
    		- Understand producer as well as consumer APIs
    		- Create new project with KafkaWorkshop
    		- Define dependencies in build.sbt
    		- Create application.properties for passing zookeeper and kafka broker information
    		- Use producer API to produce messages to Kafka topic
   	 	- Use consumer API to consume messages from Kafka topic

* REVISION ABOUT KAFKA TOPICS

	- Kafka topic is nothing but group of files into which messages are added using Producer API and consumed using Consumer API.
		- Topic can be partitioned, for each of the partition there will be directory in the nodes where Kafka broker is running.
    		- Based on the replication factor each directory related to partition will be mirrored to more than one node on which Kafka broker is running.
    		- We can use 3rd party applications such as Kafka Connect, Flume, Logstash etc to publish messages to topic or use Producer API and produce messages using any programming language.
    		- We can use 3rd party applications such as Kafka Connect, Kafka Streams, Flume, Spark Streaming etc to consume messages or use Consumer API with any programming language.
    		- One or more producers can produce messages to same topic. We can also develop different producers produce messages to different partitions in the same topic.
    		- We can have different consumers or consumer groups consume messages from the same topic in round robin fashion.
    		- We can also have different consumers or consumer group consume messages from different partitions

* PRODUCER AND CONSUMERS APIs

	- Let us understand the details with respect to Producer and Consumer APIs.
    		- Both Producer and Consumer APIs comes as part of Kafka client jar file
    		- Main package for Kafka client is org.apache.kafka.clients
    		- In that we have sub packages for producer as well as consumer
    		- org.apache.kafka.clients.producer have APIs related to creating Kafka producer object, defining producer configurations etc
    		- org.apache.kafka.clients.consumer have APIs related to creating Kafka consumer object, defining consumer configurations etc
    		- As we have to write objects into files (Kafka topic) we have to serialize and deserialize
    		- APIs related to serialization and deserialization are available under org.apache.kafka.common package

* Create Project – KafkaWorkshop

	- Let us create a project from the scratch.
    		- Click on New Project
    		- Choose Scala as programming language and SBT as build tool
    		- Make sure you made these choices
        		- Project Name – KafkaDemo
        		- JDK – 1.8
        		- SBT – 0.13.x
        		- Scala – 2.11
        		
* Define Dependencies

	- Now let us define dependencies as part of build.sbt to download and use required binaries to build simple applications to produce and consume messages using Scala as programming language.
    		- We need to import few APIs that are part of org.apache.kafka
    		- There are several jar files under kafka
    		- For Producer/Consumer APIs it is required to import kafka-clients
    		- Add the dependency of relevant version in build.sbt – libraryDependencies += "org.apache.kafka" % "kafka-clients" % "1.0.0"

		- $ sbt console
		- scala> import org.apache.kafka
		- scala> import org.apache.kafka.clients
		- scala> import org.apache.kafka.clients.producer.{KafkaProducer,ProducerConfig,ProducerRecord}
		- scala> import org.apache.kafka.clients.consumer.{KafkaConsumer,ConsumerConfig}

* Externalize Properties

	- We need to connect to Kafka using Kafka broker/bootstrap server. We typically connect to local broker/bootstrap server in development process and actual production cluster in production. 
	- It is better to externalize these properties and use them at run time.


    	- We will use com.typesafe config package to load externalized properties at run time.
    	- Update build.sbt with appropriate dependency – libraryDependencies += "com.typesafe" % "config" % "1.3.2"
    	- Here is the build.sbt after adding Kafka and typesafe config dependencies.

name := "KafkaWorkshop"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "1.0.0"

    	- There are multiple ways to externalize properties
        	- Bundle properties related to all the environments as part of the code base/compiled jar file
        	- Place properties file at the same location in all environments
        	- In the former approach we need to categorize property names based on environment they are pointing to, while in later approach we have same property names but different values pointing to respective environment.
        	- For demo purpose we will bundle the properties file along with the code.
    	- Create resources folder under src/main
    	- Create file by name application.properties
    	- Add all the properties related to dev and prod. You can add more categories as well.

dev.zookeeper = localhost:2181

dev.bootstrap.server = localhost:9092

prod.zookeeper = nn01.itversity.com:2181,nn02.itversity.com:2181,nn03.itversity.com:2181

prod.bootstrap.server = wn01.itversity.com:6667,wn02.itversity.com:6667


import com.typesafe.config.ConfigFactory

object KafkaProducerExample {
	
	def main(args: Array[String]) : Unit = {
		val conf = ConfigFactory.load
		
		// Program arguments: dev
		val envProps = conf.getConfig(args(0))
		println(envProps.getString("zookeeper"))
	}
}


* Produce Messages – Producer API

	- Now it is time to develop our first program leveraging Producer API to write messages to Kafka topic.
	
	- kafka-topics --zookeeper quickstart.cloudera:2181 --list
	- cd /home/cloudera/IdeaProjects/KafkaWorkshop/building-streaming-data-pipelines-using-kafka-and-spark
	- sbt console
	- scala> import.java.util.Properties
	- scala> import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
	- scala> import com.typesafe.config.ConfigFactory
	- scala> val props = new Properties()
	- scala> val conf = ConfigFactory.load
    - scala> val envProps = conf.getConfig(args(0))
	- scala> props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getString("bootstrap.server"))
    - scala> props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducerExample")
    - scala> props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    - scala> props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    - scala> val producer = new KafkaProducer[String, String](props)
    - scala> val data = new ProducerRecord[String, String]("Kafka-Testing", "Key", "Value")
    - scala> producer.send(data)
    - kafka-console-consumer --bootstrap-server quickstart.cloudera:9092 --topic Kafka-Testing --from-beginning

    	- Right click on src/main/scala -> new -> Scala Class
    	- Change kind to Object
    	- Give name as KafkaProducerExample and hit enter. A new file will created with object.
    	- Define main function
    	- Import java.util.Properties – import java.util.Properties – it is useful to configure necessary properties while creating KafkaProducer object which can be used to produce messages into Kafka topic.
    	- Import ConfigFactory which will be used to load the application.properties file to get Kafka broker/bootstrap server information – import com.typesafe.config.ConfigFactory
    	- Import classes related to Producer API to write messages to Kafka topic – import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
    	- We need ProducerConfig to get enums related to setting properties to connect to Kafka topic
    	- Create Properties object and add properties related to broker/bootstrap servers as well as serializer to serialize messages to write into Kafka topic
    	- Once the required properties are added we can create KafkaProducer object – val producer = new KafkaProducer[String, String](props)
    	- We can now create ProducerRecord object by passing topic as well as key and value for the message – val data = new ProducerRecord[String, String]("Kafka-Testing", "Key", "Value")
    	- We can send ProducerRecord objects using send API on producer (KafkaProducer object) – producer.send(data)
    	- Once you send all the messages, make sure to close producer – producer.close()
    	- Here is the complete code example to produce messages.


import java.util.Properties

import com.typesafe.config.ConfigFactory

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object KafkaProducerExample {

  def main(args: Array[String]): Unit = {
  
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getString("bootstrap.server"))
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducerExample")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)

    val data = new ProducerRecord[String, String]("Kafka-Testing", "Key", "Value")
    producer.send(data)
    producer.close()
  }
  
}       		