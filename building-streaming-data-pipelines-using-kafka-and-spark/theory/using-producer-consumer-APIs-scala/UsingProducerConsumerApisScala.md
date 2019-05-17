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