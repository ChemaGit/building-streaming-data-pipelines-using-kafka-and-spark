package hbaseapplicationdevelopmentlifecycle.hbasedemo

import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.util.Bytes
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.{Row, SparkSession}

/**
  * Created by itversity on 27/08/18.
  */

object NYSELoadSpark {

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

  def buildPutList(table: Table, nyseRecord: Row) = {
    val put = new Put(Bytes.toBytes(
      nyseRecord.getString(1).substring(0,6)
        + "," + nyseRecord.get(0))) // Key

    put.addColumn(Bytes.toBytes("sd"),
      Bytes.toBytes(nyseRecord.get(1) + ",op"),
      Bytes.toBytes(nyseRecord.getString(2)))
    put.addColumn(Bytes.toBytes("sd"),
      Bytes.toBytes(nyseRecord.get(1) + ",hp"),
      Bytes.toBytes(nyseRecord.getString(3)))
    put.addColumn(Bytes.toBytes("sd"),
      Bytes.toBytes(nyseRecord.get(1) + ",lp"),
      Bytes.toBytes(nyseRecord.getString(4)))
    put.addColumn(Bytes.toBytes("sd"),
      Bytes.toBytes(nyseRecord.get(1) + ",cp"),
      Bytes.toBytes(nyseRecord.getString(5)))
    put.addColumn(Bytes.toBytes("sd"),
      Bytes.toBytes(nyseRecord.get(1) + ",v"),
      Bytes.toBytes(nyseRecord.getString(6)))
    put
  }

  def main(args: Array[String]): Unit = {
    val env = args(0)
    val conf = ConfigFactory.load.getConfig(env)

    val spark = SparkSession.
      builder.
      master(conf.getString("execution.mode")).
      appName("NYSE Load using Spark").
      getOrCreate()

    val nyseData = spark.read.csv(args(1))
    nyseData.foreachPartition(records => {
      val connection = getHbaseConnection(conf, env)
      val table = connection.
        getTable(TableName.valueOf("nyse:stock_data_wide"))
      records.foreach(record => {
        val row = buildPutList(table, record)
        table.put(row)
      })
      table.close
      connection.close
    })
  }
}