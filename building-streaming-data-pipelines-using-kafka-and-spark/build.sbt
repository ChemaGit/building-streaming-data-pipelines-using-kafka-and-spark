name := "building-streaming-data-pipelines-using-kafka-and-spark"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "1.0.0"

libraryDependencies += "com.maxmind.geoip2" % "geoip2" % "2.12.0"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"

libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.2.0"

libraryDependencies += "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % "2.2.0"

dependencyOverrides += "org.apache.spark" % "spark-sql_2.11" % "2.2.0"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.5"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.9.5"


// /home/cloudera/.ivy2/cache/com.typesafe/config/bundles/config-1.3.2.jar
// /home/cloudera/.ivy2/cache/org.apache.spark/spark-sql_2.11/jars/spark-sql_2.11-2.3.0.jar
// /home/cloudera/.ivy2/cache/org.apache.kafka/kafka-clients/jars/kafka-clients-1.0.0.jar
// /home/cloudera/.ivy2/cache/org.apache.spark/spark-sql-kafka-0-10_2.11/jars/spark-sql-kafka-0-10_2.11-2.3.0.jar
// /home/cloudera/.ivy2/cache/org.apache.spark/spark-sql_2.11/jars/spark-sql_2.11-2.1.0.jar
// /home/cloudera/.ivy2/cache/org.apache.spark/spark-core_2.11/jars/spark-core_2.11-2.3.0.jar