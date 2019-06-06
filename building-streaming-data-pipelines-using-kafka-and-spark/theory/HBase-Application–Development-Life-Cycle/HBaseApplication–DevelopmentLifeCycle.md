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