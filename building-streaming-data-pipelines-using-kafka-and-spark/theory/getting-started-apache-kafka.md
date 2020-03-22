## Let us understand the concepts behind Kafka to get data from sources to targets in real time.
````text
- Setup Datasets
- Setup Kafka locally
- Kafka on multi-node cluster
- Apache Kafka – Overview
- Apache Kafka – Glossary or Concepts
- Zookeeper Commands – Overview
- Producer and Consumer – Different Scenarios
- Messages and Message Format
````

### Setup gen_logs
````text
- One of the common use case for streaming pipelines is to understand visitor traffic. 
  gen_logs is a simple Python based application which can be used to generate log messages as if some one is accessing an eCommerce website. 
- Click "https://github.com/dgadiraju/gen_logs" for the GitHub repository.
    - It has shell scripts which invoke python program
    - It is compatible with Python 2
````

### Here is the setup process on linux or mac or on cygwin. On Windows we are going to use Ubuntu setup using Windows Subsystem for Linux.
````text
- Identify directory /opt/
- Copy gen_logs directory to /opt – sudo mv -f gen_logs /opt
- Run sudo chown -R `whoami` /opt/gen_logs
- Update PATH in .profile or .bash_profile or create soft links for the shell programs – export PATH=$PATH:/opt/gen_logs
- Either restart shell or run .profile/.bash_profile script
- Run start_logs.sh to start generating web logs
- Run tail_logs.sh to preview while logs are being generated (Hit ctrl-c to come out)
- Run stop_logs.sh to stop generating web logs
````
````bash
$ git clone https://github.com/dgadiraju/gen_logs.git
$ sudo cp -rf gen_logs /opt
$ sudo chown -R user:pass gen_logs
$ sudo ln -s /opt/get_logs/start_logs.sh /usr/bin/.
$ sudo ln -s /opt/get_logs/tail_logs.sh /usr/bin/.
$ sudo ln -s /opt/get_logs/stop_logs.sh /usr/bin/.

# run logs
$ start_logs.sh
$ tail_logs.sh
$ stop_logs.sh
````

### SETUP KAFKA LOCALLY
````text
- For Windows make sure you have Ubuntu setup using subsystem.
- Let us review steps for setup process.
- Download Kafka binaries (compressed tar ball)
- Untar and uncompress tar ball (creates a directory)
- Create soft link pointing to the directory created in last step. This will come handy in case of upgrades.
- Kafka require Zookeeper to be up and running. We will understand the role of Zookeeper at later point in time.
- Review the Zookeeper properties and start Zookeeper server.
- Review Kafka properties and start Kafka server.
- Once you finalize the directory, steps are same irrespective of the platform we are using. We will be using /opt as the directory.
- Go to https://kafka.apache.org/downloads
- Choose latest version built with Scala 2.11 (1.0.0)
- Click on the link and go to mirror for kafka_2.11-1.0.0.tgz. Run this command under /opt – sudo wget http://apache.mirrors.ionfish.org/kafka/1.0.0/kafka_2.11-1.0.0.tgz (I have already downloaded on my PC)
- Untar the tar ball under /opt sudo tar xzf kafka_2.11-1.0.0.tgz
- Create soft link with name /opt/kafka – ln -s /opt/kafka_2.11-1.0.0 /opt/kafka
- Update PATH as part of .profile or .bash_profile for user kafka and training export PATH=$PATH:/opt/kafka/bin
- Starting Kafka – Kafka requires
    - Zookeeper
    - Kafka Server
- Here are the commands you need to run (provided PATH is updated)
- Make sure you are connected as kafka
- Starting zookeeper-server: zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties
- Starting kafka-server: kafka-server-start.sh -daemon "/opt/kafka/config/server.properties"
- Use -daemon to submit zookeeper and kafka servers in background
````
````bash
$ cd /opt
$ sudo wget http://mirrors.fibergrid.in/apach/kafka/2.0.0/kafka_2.12-2.0.0.tgz
$ sudo tar xzf kafka_2.12-2.0.0.tgz
$ sudo chown -R user:pass kafka_2.12-2.0.0
$ sudo ln -s kafka_2.12-2.0.0 kafka
$ cd 
$ vi .profile
export PATH=$PATH:/opt/kafka/bin
:x
$ . ~/.profile
$ cd /opt/kafka/config
$ view zookeeper.properties
$ cd 
$ zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties
$ telnet localhost 2181
$ cd /opt/kafka/logs
$ ls -ltr
$ view server.log
$ cd /opt/kafka/config
$ view server.properties
$ cd
$ kafka-server-start.sh -daemon /opt/kafka/config/server.properties
$ telnet localhost 9092
````

### VALIDATING KAFKA
````text
- As we are done with setting up of Kafka locally, let us go ahead and validate Kafka.
    - As part of setting up of Kafka, you will get bunch of shell scripts under bin. We have already seen few while starting zookeeper and kafka earlier. There are several other commands
        - zookeeper-server-stop.sh
        - kafka-server-stop.sh
        - kafka-topics.sh
        - kafka-console-producer.sh
        - kafka-console-consumer.sh
    - There are bunch of other commands as well.
    - In actual implementation we will not use these commands to manage topics or produce/consume messages from topic. However these commands come handy for quick validations and troubleshooting certain issues related to topics.
    - We will manage topics, publish messages into topic as well as consume messages from topic programmatically, which means we will be using respective APIs in programming languages such as Java, Scala, Python etc.
    - Each of these commands use different details to connect to the cluster. kafka-topics.sh uses zookeeper while other 2 uses kafka broker details.
    - Create topic: 
````
````bash
kafka-topics.sh --zookeeper localhost:2181 --create --topic test --partitions 1 --replication-factor 1


# When we create topic, it will create directory under /tmp/kafka-logs (review log.dirs under server.properties) 
# using the name of topic and partition index (e.g.: test-0)

# List topic: 
$ kafka-topics.sh --zookeeper localhost:2181 --list

# Publish Messages: 
$ kafka-console-producer.sh --broker-list localhost:9092 --topic test

# From other window Consume Messages: 
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning

# Deleting topic: 
$ kafka-topics.sh --zookeeper localhost:2181 --delete --topic test

$ cd /opt/kafka/bin
$ ls -ltr
$ kafka-topics.sh
$ kafka-topics.sh --zookeeper localhost:2181 --list
$ kafka-topics.sh --zookeeper localhost:2181 --create --topic test --partitions 1 --replication-factor 1
$ kafka-topics.sh --zookeeper localhost:2181 --list
$ kafka-topics.sh --zookeeper localhost:2181 --describe --topic test
$ kafka-console-producer.sh
$ kafka-console-producer.sh --broker-list localhost:9092 --topic test
$ kafka-console-consumer.sh
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test 
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
````

### VALIDATING WITH GEN_LOGS TRAFFIC
````text
- Now let us validate using data generated by gen_logs application streaming into the topic by using command line approach (available under bin directory of kafka).

- Make sure zookeeper and kafka broker are running (using telnet command)
- Make sure gen_logs are generating log messages simulating visitor traffic
- We will pipe the output of tail_logs.sh to kafka-console-producer.sh command and then use kafka-console-consumer.sh to consume message from the topic.
    - Topic Details
    - Name: retail
    - Partitions: 3
    - Replication Factor: 1 (as kafka broker is only running on one node, we cannot have replication factor higher than 1)
````
````bash
# Create topic: 
kafka-topics.sh --zookeeper localhost:2181 --create --topic retail --partitions 3 --replication-factor 1

# List topic: 
kafka-topics.sh --list --zookeeper localhost:2181

# Publish Messages: 
tail_logs.sh|kafka-console-producer.sh --broker-list localhost:9092 --topic retail

# From other window Consume Messages: 
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic retail --from-beginning

$ telnet localhost 2181
$ zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties
$ telnet localhost 9092
$ kafka-server-start.sh -daemon /opt/kafka/config/server.properties
$ telnet> quit
$ kafka-topics.sh --zookeeper localhost:2181 --create --topic retail --partitions 3 --replication-factor 1
$ ls -ltr /tmp/kafka-logs/retail-0
$ ls -ltr /tmp/kafka-logs/retail-1
$ ls -ltr /tmp/kafka-logs/retail-2
$ tail_logs.sh
$ start_logs.sh
$ tail_logs.sh | kafka-console-producer.sh --broker-list localhost:9092 --topic retail
$ ls -ltr /tmp/kafka-logs/retail-0
$ ls -ltr /tmp/kafka-logs/retail-1
$ ls -ltr /tmp/kafka-logs/retail-2
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic retail
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic retail --from-beginning
````

### KAFKA ON MULTI-NODE CLUSTER
````text
- Before going into the details about Apache Kafka, let us review our Big Data developer labs where we have multi node Kafka broker setup along with other Big Data tools such as Hadoop, Spark etc.

- Kafka Cluster Components
    - Cluster is setup using Hortonworks distribution, which means we can preview the cluster using web UI called as Ambari.
    - Cluster topology
    - Gateways – gw02.itversity.com and gw03.itversity.com
    - Zookeeper Ensemble – nn01.itversity.com, nn02.itversity.com and rm01.itversity.com
    - Kafka brokers – wn01.itversity.com to wn05.itversity.com
    - Other Services – Hadoop, Spark, HBase etc.
- Topic
    - Distributed log file (distribution is achieved via partitions)
    - Each partition is nothing but directory named after topic and appended by partition index
    - In a multinode cluster we use higher replication-factor so that we can have multiple copies of each of the partition for fault tolerance.
    - Each of these copies are managed by brokers. As there will be multiple brokers associated with each partition of a given topic, one of the broker will be designated as leader.
````
````bash
$ kafka-topics.sh \
--zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
--create \
--topic retail \
--partitions 3 \
--replication-factor 2

$ kafka-topics.sh \
--zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
--list \
--topic retail

$ kafka-topics.sh \
--zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
--describe \
--topic retail

tail_logs.sh|kafka-console-producer.sh \
--broker-list wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail

$ kafka-console-consumer.sh \
--bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail \
--from-beginning
````
````text
- Producers connect to one or more brokers and push messages to topics via leader.
- Consumers pull message from topic by polling topic at regular intervals. 
  Each time consumer read messages it need to keep track of offset (can be done using multiple ways)
- Role of Zookeeper
    - Create and manage topics (a topic is nothing but distributed log file with one or more partitions)
    - Zookeeper understand load on the Kafka brokers and take care of creation of partitions as part of nodes on which brokers are running.
    - Zookeeper will also assign leader for each of the partition.
    - Manage consumer offsets
- Role of Gateway nodes
    - Developers can troubleshoot issues from gateway nodes
    - Plugins which produce messages to Kafka topic or consume messages from Kafka topic can be deployed on gateway nodes.
````

### UNDERSTANDING ZOOKEEPER AND KAFKA PROPERTIES
````text
- Let us spend some time in understanding Zookeeper as well as Kafka configuration properties. 
- You can do it using command line or UI. If the cluster is setup using distributions such as Hortonworks or Cloudera, 
  we can use Ambari or Cloudera Manager to review the properties.


- For local installation – you can go to $KAFKA_HOME/config directory and visit zookeeper.propeties and server.properties to review some of the important properties.
- For multinode installation, you will have these files on all the nodes and hence reviewing them via command line is not reliable.
- Whatever management tools you have you should be able to review the properties through their interfaces. 
  In our case we will review the cluster managed by Ambari
- You can go to zookeeper configs and kafka configs to review the properties
- Also let us understand some of the important properties with respect to zookeeper and kafka
````
````bash
$ cd /opt/kafka/
$ cd config
$ view zookeeper.properties
$ view server.properties
````

### APACHE KAFKA - OVERVIEW
````text
- Apache Kafka is an open-source stream-processing software platform developed by the Apache Software Foundation, written in Scala and Java. 
- It was initially developed as an internal product at LinkedIn and was open-sourced and adopted by apache foundation.
- Named after author Franz Kafka
- Salient Features:
    - Highly Scalable (partitioning)
    - Fault Tolerant (replication factor)
    - Low Latency
    - High Throughput
````

### Kafka eco system
````text
- Heart of Kafka is topic a distributed and fault tolerant log file. However over a period of time Kafka is evolved into eco system of tools.
    - Kafka Connect
    - Kafka Streams and Kafka SQL
    - Producer and Consumer APIs
    - 3rd party plugins to integrate with Flume, logstash, Spark Streaming, Storm, Flink etc.
````

### Kafka Use cases
````text
- As micro services have evolved Kafka become popular to integrate data between different micro services – asynchronous, real time as well as batch.
    - Activity Tracking: Kafka was originally developed for tracking user activity on LinkedIn
    - Messaging: Kafka is also used for messaging, where applications need to send notifications (such as emails) to users.
    - Metrics and logging: Applications publish metrics on a regular basis to a Kafka topic, 
      and those metrics can be consumed by systems for monitoring and alerting.
    - Commit log: database changes can be published to Kafka and applications can easily monitor this stream to receive live updates as they happen. 
      This changelog stream can also be used for replicating database updates to a remote system.
    - Stream processing: Kafka can be integrated with stream frameworks such as Spark Streaming, Flink, Storm etc. 
      Users are allowed to write applications to operate on Kafka messages, performing tasks such as counting metrics, transform data, etc.
````

### Glossary
````text
- Topic: A topic represent group of files and directories. When we create topic, it will create directories with topic name and partition index. 
  These directories have bunch of files which will actually store the messages that are being produced.
- Publisher or Producer: Publishers or producers are processes that publish data (push messages) to the log file associated with Kafka topic.
- Subscriber or Consumer: Subscribers or consumers are processes that read from the log file associated with Kafka topic
````

### Kafka Pub Sub Model
````text
- Partition: Kafka topics are divided into a number of partitions, which contains messages in an unchangeable sequence. 
    - This allows for multiple consumers to read from a topic in parallel.

- Leader: When we create Kafka topic with partitions and replication factor, each partition will have leader. 
    - Messages will be first written to the partition on broker which is designated as leader and then copied to rest of followers.

- Replication Factor: Each partition can be cloned into multiple copies using replication factor. It will facilitate fault tolerance. 
    - With replication factor of n on m node cluster (where n <= m), cluster can survive the failure of n-1 nodes at any point in time.

- Broker: A Kafka cluster consists of one or more servers (Kafka brokers), which are running Kafka. 
    - Producers query metadata of each of the topic and connect to leader of each partition to produce messages into Kafka topic. 
    - Consumers do the same while consuming messages from the topic.

- Offset: The records in the partitions are each assigned a sequential id number called the offset that uniquely identifies each record within the partition.
````

````bash
$ kafka-topics.sh --zookeeper quickstart.cloudera:2181 --describe --topic retail

# Topic:retail	PartitionCount:3	ReplicationFactor:1	Configs:
#    Topic: retail	Partition: 0	Leader: 38	Replicas: 38	Isr: 38
#    Topic: retail	Partition: 1	Leader: 38	Replicas: 38	Isr: 38
#    Topic: retail	Partition: 2	Leader: 38	Replicas: 38	Isr: 38


$ tail_logs.sh | kafka-console-producer.sh --broker-list quickstart.cloudera:9092 --topic retail
$ kafka-conslole-consumer.sh bootstrap-servers quickstart.cloudera:9092 --topic retail
$ cd /opt/kafka/kafka-logs/retail-0
$ view 000###.log
$ view 000###.index
````

### PRODUCER AND CONSUMER - DIFFERENT SCENARIOS
````text
- Let us see few scenarios related to producing and consuming messages using Kafka commands. 
  It will help us understanding concepts around using topics effectively.

- Broker id and Host mapping (subject to change and only for reference)
    - wn01.itversity.com – 1003
    - wn02.itversity.com – 1005
    - wn03.itversity.com – 1002
    - wn04.itversity.com – 1001
    - wn05.itversity.com – 1004
````

### Single Partition topic – One Consumer
````bash
# Now let us create topic with single partition and then produce messages into the topic and also consume from it using one consumer.
# One producer – Topic with Single partition – One consumer

$ kafka-topics.sh \
--zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
--create \
--topic retail_single \
--partitions 1 \
--replication-factor 2

$ kafka-topics.sh \
--zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
--describe \
--topic retail_single

$ tail_logs.sh|kafka-console-producer.sh \
--broker-list wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail_single

$ kafka-console-consumer.sh \
--bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail_single \
````

### Single Partition Topic – Multiple Consumers
````bash
# Now let us see few scenarios where data is being produced into a topic with single partition and consumed by multiple consumers.
# One producer – Topic with Single partition – Multiple consumers with in same group

$ tail_logs.sh|kafka-console-producer.sh \
--broker-list wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail_single

$ kafka-console-consumer.sh \
--bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail_single \
--group 1

# In another session
$ kafka-console-consumer.sh \
--bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail_single \
--group 1

# One producer – Topic with Single partition – Multiple consumer groups

$ kafka-console-consumer.sh \
--bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail_single \
--group 1

# In another session
$ kafka-console-consumer.sh \
--bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
--topic retail_single \
--group 2

# At any given point in time only one consumer can consume data from a partition.
````

### Multiple Partition Topic – Single Consumer
````text
- Now let us create topic with multiple partitions and then see how data is produced into it as well as consume from it using Single Consumer.
    - By default producer will write into all partitions in round robin fashion. 
      But Producer API provide us capability to write into specific partition or based on custom map logic between data and partition. 
      We will explore those when we build Kafka applications using supported programming languages such as Scala, Python etc.
    - kafka-console-producer.sh can write to topic only in round robin fashion.
    - One producer – Topic with multiple partitions – One consumer
````
````bash
$ kafka-topics.sh \
    --zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
    --create \
    --topic retail_multi \
    --partitions 4 \
    --replication-factor 2

$ kafka-topics.sh \
    --zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
    --describe \
    --topic retail_multi

$ tail_logs.sh | kafka-console-producer.sh \
    --broker-list wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi

$ kafka-console-consumer.sh \
    --bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi \
    --group cg

$ kafka-console-consumer.sh \
    --bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi \
    --group cga

$ kafka-console-consumer.sh \
    --bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi \
    --group cga
````

### Multiple Partition Topic – Multiple Consumers
````bash
# Now let us create topic with multiple partitions and then see how data is produced into it as well as consume from it using Multiple Consumers.

$ kafka-topics.sh \
    --zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
    --create \
    --topic retail_multi \
    --partitions 4 \
    --replication-factor 2

$ kafka-topics.sh \
    --zookeeper nn01.itversity.com:2181,nn02.itversity.com:2181,rm01.itversity.com:2181 \
    --describe \
    --topic retail_multi
````
````text
- One producer – Topic with multiple partitions – Multiple consumers in one group
    - All consumers in the group will read data from partitions in tandem and in round robin fashion.
    - Each consumer will write the offset to a centralized location.

- One producer – Topic with multiple partitions – One consumer per partition in one group

- One producer – Topic with multiple partitions – One consumer for p0 and p3, one consumer for p1 and p2. 
  This combination is not possible with kafka-console-consumer but we can take care of it using consumer APIs as part of our applications to consume the data.
````
````bash
$ tail_logs.sh | kafka-console-producer.sh \
    --broker-list wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi

$ kafka-console-consumer.sh \
    --bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi \
    --group cgb \
    --partition 1

$ kafka-console-consumer.sh \
    --bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi \
    --group cgb \
    --partition 2

$ kafka-console-consumer.sh \
    --bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi \
    --group cgb \
    --partition 3

$ kafka-console-consumer.sh \
    --bootstrap-server wn01.itversity.com:6667,wn02.itversity.com:6667 \
    --topic retail_multi \
    --group cgb \
    --partition 4
````

### Consumers and Consumer groups
````text
- Consumers label themselves with a consumer group name, 
  and each record published to a topic is delivered to one consumer instance within each subscribing consumer group.
- This process of maintaining membership in the group is handled by Kafka dynamically. 
- If new instances join the group they will take over some partitions from other members of the group; 
- if an instance dies, its partitions will be distributed to the remaining instances.
- To consume data in parallel from topic which is partitioned, we need to have separate consumer for each partition. 
- At any given point in time more than one consumer cannot consume data from same partition.
````

### Messages and Message Format
````text
- Now let us recap how messages are produced to topic and consumed from topic. 
  Also we will look into the details about format related to Message batch and Message. 
  Message is also synonymous to Record.
    - Producer produces messages to Kafka topic in batches for performance reasons.
    - All the messages are appended to the log files associated with partitions.
    - We can have key for each message (using APIs). Keys are used in partitioning the data as well as log compaction if the logs have to be replayed.
    - Partitions are allocated to the message either in round robin fashion (when there is no key) or based on hash mod value on key.
    - Consumer periodically poll into Kafka topic partition using offset and read messages continuously from the topic.
    - As messages are produced in batches, 
      there will be some information for each batch as well as some information for each message with in batch written to log file 
      (along with actual messages)

- Advantages of batching
    - As part of producing messages we can send larger TCP packets leveraging network bandwidth. 
      It is always better in performance compared to sending individual messages and waiting for acknowledgements.
    - Compression rates will be much higher with larger batch of messages than individual messages.
    - When batching is used producer will take care of grouping messages for each partition and append 
      to the log file associated with the partition by talking to the leader of respective partitions.
    - Even while consuming data it is not advisable to consume individual messages and commit offset for each message. 
      Performance will be poor.
    - As we typically consume multiple messages in each iteration, 
      our consumers should have necessary logic to deal with errors in such a way that same data is not duplicated in the target system.
````

