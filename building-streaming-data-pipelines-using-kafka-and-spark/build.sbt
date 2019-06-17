name := "building-streaming-data-pipelines-using-kafka-and-spark"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "1.0.0"
libraryDependencies += "org.apache.kafka" %% "kafka" % "1.1.0"

libraryDependencies += "com.maxmind.geoip2" % "geoip2" % "2.12.0"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.2.0"
libraryDependencies += "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % "2.2.0"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.2.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.2.0"

libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.0"
libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.7.0"

libraryDependencies += "org.apache.hbase" % "hbase-client" % "1.1.8"
libraryDependencies += "org.apache.hbase" % "hbase-common" % "1.1.8"
libraryDependencies += "org.apache.hbase" % "hbase-server" % "1.1.8"
libraryDependencies += "org.apache.hbase" % "hbase-protocol" % "1.1.8"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.5"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.9.5"

/*assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.startsWith("META-INF") => MergeStrategy.discard
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", xs@_*) => MergeStrategy.first
  case "about.html" => MergeStrategy.rename
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}
  *********************************
  Under directory project we have to create the assembly.sbt file and add 
  the depencency 
  addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
  now from command line run $ sbt assembly
  /target/scala-2.11/building-streaming-data-pipelines-using-kafka-and-spark-assembly-0.1.jar 
  run the application
  $ sbt assembly
  $ java -jar target/scala-2.11/building-streaming-data-pipelines-using-kafka-and-spark-assembly-0.1.jar dev /home/cloudera/files/NYSE_2010.txt nyse:stock_data_thin thin
 */

assemblyMergeStrategy in assembly := {
  case (m: String) if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case (m: String) if m.startsWith("META-INF") => MergeStrategy.discard
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", xs@_*) => MergeStrategy.first
  case "about.html" => MergeStrategy.rename
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

mainClass in assembly := Some("hbaseapplicationdevelopmentlifecycle.hbasedemo.NYSELoad")  

/*
lazy val root = (project in file(".")).
  settings(
    mainClass in compile := Some("hbaseapplicationdevelopmentlifecycle.hbasedemo.NYSELoadSpark")
  )
 */
// dependencyOverrides += "org.apache.spark" % "spark-sql_2.11" % "2.2.0"
// /home/cloudera/.ivy2/cache/com.typesafe/config/bundles/config-1.3.2.jar
// /home/cloudera/.ivy2/cache/org.apache.spark/spark-sql_2.11/jars/spark-sql_2.11-2.3.0.jar
// /home/cloudera/.ivy2/cache/org.apache.kafka/kafka-clients/jars/kafka-clients-1.0.0.jar
// /home/cloudera/.ivy2/cache/org.apache.spark/spark-sql-kafka-0-10_2.11/jars/spark-sql-kafka-0-10_2.11-2.3.0.jar
// /home/cloudera/.ivy2/cache/org.apache.spark/spark-sql_2.11/jars/spark-sql_2.11-2.1.0.jar
// /home/cloudera/.ivy2/cache/org.apache.spark/spark-core_2.11/jars/spark-core_2.11-2.3.0.jar