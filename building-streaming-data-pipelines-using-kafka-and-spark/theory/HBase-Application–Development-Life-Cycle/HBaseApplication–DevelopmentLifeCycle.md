# HBase Application – Development Life Cycle

	- As we understood basics of HBase, now let us look at how we can use Scala as well as Spark Dataframes to build applications using HBase as database.

    	- Revision of HBase Shell (CRUD Operations)
    	- Setup Project
    	- Put and Get examples using Scala
    	- Develop GettingStarted using Scala
    	- Develop NYSELoad using Scala
    	- Develop NYSELoadSpark using Spark Data Frames
    	- Advanced Querying using HBase Shell
    	- Advanced Querying Programmatically

# Revision of HBase Shell (CRUD Operations)

	- HBase Shell is CLI to manage tables and run queries for validation and exploratory purposes.


    	- We can list the tables using list command
    	- We can create namespace by using create_namespace command
    	- We can create table by using create ‘namespace:table’, ‘columnfamily’
        	- Create table for GettingStarted – create 'training:hbasedemo', 'cf1'
        	- Create table for NYSELoad – create 'nyse:stock_data', 'sd'
        	- Create table for NYSELoadSpark – create 'nyse:stock_data_wide', 'sd'
    	- We can perform CRUD operations
        	- Create or Update – put
        	- Read – scan or get
        	- Delete – delete

# Setup Project

	- Here are the steps involved to setup the project

    	- Make sure necessary tables is created (training:hbasedemo, nyse:stock_data, nyse:stock_data_wide)
    	- Create new project HBaseDemo using IntelliJ
        	- Choose scala 2.11
        	- Choose sbt 0.13.x
        	- Make sure JDK is chosen
    	- Update build.sbt. See below
    	- Define application properties

# Dependences (build.sbt)

	- HBase applications are dependent upon Hadoop and hence we need to add dependencies related to Hadoop as well as HBase.

    	- Add type safe config dependency so that we can externalize properties
    	- Add hadoop dependencies
    	- Add hbase dependencies
    	- Define merge strategy. It is required to build fat jar so that we can deploy and run on other environments.
    	- Replace build.sbt with below lines of code

name := "HBaseDemo"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.0"

libraryDependencies += "org.apache.hbase" % "hbase-client" % "1.1.2"

libraryDependencies += "org.apache.hbase" % "hbase-common" % "1.1.2"

assemblyMergeStrategy in assembly := {

  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.startsWith("META-INF") => MergeStrategy.discard
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", xs@_*) => MergeStrategy.first
  case "about.html" => MergeStrategy.rename
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

# Externalize Properties

	- We need to make sure that application can be run in different environments. It is very important to understand how to externalize properties and pass the information at run time.

    		- Make sure build.sbt have dependency related to type safe config
    		- Create new directory under src/main by name resources
    		- Add file called application.properties and add below entries

dev.zookeeper.quorum = localhost

dev.zookeeper.port = 2181

prod.zookeeper.quorum = nn01.itversity.com,nn02.itversity.com,rm01.itversity.com

prod.zookeeper.port = 2181

# Put and Get Examples (using sbt console)

	- As we have added necessary dependencies we can use sbt console to launch scala with all dependencies made available to scala to see examples using Scala REPL or CLI.

    	- Launch Scala REPL using sbt console
    	- Import all the necessary classes or objects or functions
    	- Create HBase connection object using zookeeper quorum and port
    	- Create table object by using appropriate table name (make sure table is pre created using hbase shell create 'training:hbasedemo'
    	- To insert a new cell
        	- Create put object
        	- Add necessary columns
        	- Add or update record using put function on table object
        	- Validate by running scan 'training:hbasedemo'
    	- To get one row by using key
        	- Create get object
        	- Get row using table.get(key)
        	- Read individual cell and pass it to functions such as Bytes.toString to typecast data to original format

			- scala> import org.apache.hadoop.conf.Configuration
			- scala> import org.apache.hadoop.hbase.client._
			- scala> import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
			- scala> import org.apache.hadoop.hbase.util.Bytes
			- scala> val hbaseConf = HBaseConfiguration.create()
			- scala> hbaseConf.set("hbase.zookeeper.quorum", "localhost")
			- scala> hbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
			- scala> val connection = ConnectionFactory.createConnection(hbaseConf)
			- scala> val table = connection.getTable(TableName.valueOf("training:hbasedemo"))

			- scala> val row = new Put(Bytes.toBytes("4"))
			- scala> row.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("column2"), Bytes.toBytes("value2"))
			- scala> row.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("column4"), Bytes.toBytes("value4"))
			- scala> table.put(row)
			- scala> table.close
			- scala> connection.close

			- scala> val key = new Get(Bytes.toBytes("4"))
			- scala> val row = table.get(key)
			- scala> val v1 = row.getValue(Bytes.toBytes("cf1"), Bytes.toBytes("column2"))
			- scala> Bytes.toString(v1)
			- scala> val v2 = row.getValue(Bytes.toBytes("cf1"), Bytes.toBytes("column4"))
			- scala> Bytes.toString(v2)
			- scala> table.close
			- scala> connection.close

			- scala> val scan = new Scan()

			- scala> val scanner = table.getScanner(scan)
			- scala> var result = scanner.next()

			- scala> while (result != null) {
			- scala>   for(cell <- result.rawCells()) {
			- scala>     println("row key:" + Bytes.toString(CellUtil.cloneRow(cell)) +
			- scala>       ":column family:" + Bytes.toString(CellUtil.cloneFamily(cell)) +
			- scala>       ":column name:" + Bytes.toString(CellUtil.cloneQualifier(cell)) +
			- scala>       ":value:" + Bytes.toString(CellUtil.cloneValue(cell)))
			- scala>   }
			- scala>   result = scanner.next()
			- scala> }

			- scala> table.close
			- scala> connection.close



# Develop GettingStarted Program

	- Now let us develop program called GettingStarted, validate using IDE, build and run on cluster.

# Create GettingStarted using IDE

	- We will create object file using IDE to develop the logic.
	- Create scala program by choosing Scala Class and then type Object
	- Make sure program is named as GettingStarted
	- First we need to import necessary APIs
	- Develop necessary logic 
		- Get the properties from application.properties
    		- Load zookeeper.quorum and zookeeper.port and create HBase connection
    		- Perform necessary operations to demonstrate


import com.typesafe.config.{Config, ConfigFactory}

import org.apache.hadoop.conf.Configuration

import org.apache.hadoop.hbase.client._

import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}

import org.apache.hadoop.hbase.util.Bytes

object GettingStarted {

  def getHbaseConnection(conf: Config, env: String): Connection ={
  
    //Create Hbase Configuration Object
    val hbaseConfig: Configuration = HBaseConfiguration.create()
    hbaseConfig.set("hbase.zookeeper.quorum", conf.getString("zookeeper.quorum"))
    hbaseConfig.set("hbase.zookeeper.property.clientPort", conf.getString("zookeeper.port"))
    if(env != "dev") {
      hbaseConfig.set("zookeeper.znode.parent", "/hbase-unsecure")
      hbaseConfig.set("hbase.cluster.distributed", "true")
    }
    val connection = ConnectionFactory.createConnection(hbaseConfig)
    connection
  }

  def main(args: Array[String]): Unit = {

    val env = args(0)
    val conf = ConfigFactory.load.getConfig(env)
    val connection = getHbaseConnection(conf, env)
    val table = connection.getTable(TableName.valueOf(args(1)))

    val put = new Put(Bytes.toBytes("2"))
    put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("column1"), Bytes.toBytes("value1"))
    put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("column2"), Bytes.toBytes("value2"))

    table.put(put)

    table.close
    connection.close()
  }
}


    	- Program takes 2 arguments, environment to load respective properties and HBase table name
    	- We can go to Run -> Edit Configurations and pass arguments
    	- If dev is passed it will try to connect to HBase installed locally otherwise it will connect to cluster specified in prod.zookeeper.quorum


# Build, Deploy and Run

	- As development and validation is done, now let us see how we can build and deploy on the cluster.

    	- Right click on the project and copy path
    	- Go to terminal and run cd command with the path copied
    	- Make sure assembly plugin is added
    	- Run sbt assembly
    	- It will generate fat jar. Fat jar is nothing but our application along with all the dependency jars integrated
    	- Copy to the server where you want to deploy
    	- Run using java -jar command – java -jar HBaseDemo-assembly-0.1.jar prod training:hbasedemo
	- Run from sbt:
		- $ sbt
		- sbt> compile
		- sbt> package
		- sbt> runMain hbaseapplicationdevelopmentlifecycle.hbasedemo.GettingStarted dev training:hbasedemo

# Develop NYSELoad using Scala

	- As part of this program we will see how we can read data from a file and load data into nyse:stock_data using Scala as programming language using HBase APIs.

    	- Read data from file (we will only process one file at a time)
    	- Create HBase Connection
    	- Create table object for nyse:stock_data
    	- For each record build put object and load into HBase table using table object (for performance reasons we can add multiple rows together)
    	- We will also see how to add main class as part of assembly, reassemble the fat jar and run it on the cluster (use sbt assembly)


name := "HBaseDemo"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.0"

libraryDependencies += "org.apache.hbase" % "hbase-client" % "1.1.8"

libraryDependencies += "org.apache.hbase" % "hbase-common" % "1.1.8"

assemblyMergeStrategy in assembly := {

  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.startsWith("META-INF") => MergeStrategy.discard
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", xs@_*) => MergeStrategy.first
  case "about.html" => MergeStrategy.rename
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

mainClass in assembly := Some("NYSELoad")


import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}

import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}

import org.apache.hadoop.hbase.util.Bytes

import com.typesafe.config.{Config, ConfigFactory}

import org.apache.hadoop.conf.Configuration

import scala.io.Source


object NYSELoad {

  def getHbaseConnection(conf: Config, env: String): Connection ={

    //Create Hbase Configuration Object
    val hbaseConfig: Configuration = HBaseConfiguration.create()
    hbaseConfig.set("hbase.zookeeper.quorum",
      conf.getString("zookeeper.quorum"))
    hbaseConfig.set("hbase.zookeeper.property.clientPort",
      conf.getString("zookeeper.port"))
    if(env != "dev") {
      hbaseConfig.set("zookeeper.znode.parent", "/hbase-unsecure")
      hbaseConfig.set("hbase.cluster.distributed", "true")
    }
    val connection = ConnectionFactory.createConnection(hbaseConfig)
    connection
  }

  def buildPutList(table: Table, nyseRecord: String, schemaType: String) = {

    val nyseAttributes = nyseRecord.split(",")
    
    val put = schemaType match {
    
      case "thin" => {
      
        val put = new Put(Bytes.toBytes(
          nyseAttributes(1) + ":" +
            nyseAttributes(0)))
        // Key
        put.addColumn(Bytes.toBytes("sd"),
          Bytes.toBytes("op"),
          Bytes.toBytes(nyseAttributes(2)))
        put.addColumn(Bytes.toBytes("sd"),
          Bytes.toBytes("hp"),
          Bytes.toBytes(nyseAttributes(3)))
        put.addColumn(Bytes.toBytes("sd"),
          Bytes.toBytes("lp"),
          Bytes.toBytes(nyseAttributes(4)))
        put.addColumn(Bytes.toBytes("sd"),
          Bytes.toBytes("cp"),
          Bytes.toBytes(nyseAttributes(5)))
        put.addColumn(Bytes.toBytes("sd"),
          Bytes.toBytes("v"),
          Bytes.toBytes(nyseAttributes(6)))
        put
      }
    }
    put
  }

  def readFilesAndLoad(table: Table, nysePath: String, schemaType: String): Unit = {

    val nyseData = Source.fromFile(nysePath).getLines()
    nyseData.foreach(record => {
      val row = buildPutList(table, record, schemaType)
      table.put(row)
    })
  }

  def main(args: Array[String]): Unit = {

    val env = args(0)
    val conf = ConfigFactory.load.getConfig(env)
    val connection = getHbaseConnection(conf, env)
    val table = connection.
      getTable(TableName.valueOf(args(2)))
    val schemaType = args(3)

    readFilesAndLoad(table, args(1), schemaType)

    table.close
    connection.close
  }
}


# Build, Deploy and Run
	- Make sure hbase table is created
	- create 'nyse:stock_data_thin', 'sd'
	- Run using Java: $ java -jar \ target/scala-2.11/HBaseDemo-assembly-0.1.jar \ prod /data/nyse/NYSE_2016.txt nyse:stock_data_thin thin
	- Run from sbt:
		- $ sbt
		- sbt> compile
		- sbt> package
		- sbt> runMain hbaseapplicationdevelopmentlifecycle.hbasedemo.NYSELoad dev /home/cloudera/files/NYSE_2017.txt nyse:stock_data_thin thin

