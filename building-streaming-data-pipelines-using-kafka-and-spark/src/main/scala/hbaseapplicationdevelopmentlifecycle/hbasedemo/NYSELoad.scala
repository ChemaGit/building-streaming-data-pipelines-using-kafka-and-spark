package hbaseapplicationdevelopmentlifecycle.hbasedemo

import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.util.Bytes
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration

import scala.io.Source

/**
  * Created by itversity on 27/08/18.
  */
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