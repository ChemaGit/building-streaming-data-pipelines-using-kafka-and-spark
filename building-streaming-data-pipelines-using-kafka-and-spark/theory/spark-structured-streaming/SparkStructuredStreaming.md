# Overview of Streaming Technologies and Spark Structured Streaming

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


# Ingestion
	
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

# Real time processing

	- As the data come through tools like logstash, flume, kafka etc we might want to perform standard transformations such as data cleansing, standardization, lookups, joins, aggregations, sorting, ranking etc. 
	- While some of the data ingestion tools are capable of some of the transformations they do not come up with all the features. 
	- Also the ingestion might get delayed and make the flow unstable. 
	- Hence we need to use the tools which are built for performing transformations as the data is streamed. Here are some of the prominent tools.
    		- Spark Streaming or Spark Structured Streaming (a module built as part of Spark)
    		- Flink
    		- Storm
    		- Kafka Streams
    		- and more

# Databases

	- Once the data is processed, we have to store data in databases to persist and build visualization layer on top of the processed data. We can use
    		- RDBMS – such as Oracle, MySQL, Postgres etc
    		- Data Warehouses – such as Teradata, Redshift etc
    		- NoSQL databases – such as HBase, Cassandra, MongoDB, DynamoDB etc
    		- Search based databases – such as Elastic Search

# Visualization

	- Visualization is typically done as part of the application development using standard frameworks.
    		- d3js
    		- Kibana
    		- Standard reporting tools such as Tableau
    		- and more

# Frameworks

	- As we discuss about different moving parts in building streaming pipelines now let us get into frameworks. Most of these frameworks do not have visualization included.
    
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

# Spark Structured Streaming

	- Apache Spark is a proven distributed computing framework with modules for different purposes
    		- Core APIs – Transformations and Actions
    		- Spark SQL and Data Frames
    		- Spark Streaming (legacy) and Spark Structured Streaming
    		- Spark MLLib
    		- Spark GraphX
    		- and more

	- We can use Spark Streaming to apply complex business rules either by using Data Frame operations or Spark SQL. 
	- Let us see a demo about getting started with Spark Structured Streaming. Main demo is done with legacy streaming.

# Important Concepts

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

# Development Life Cycle

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

# Dependencies (build.sbt)