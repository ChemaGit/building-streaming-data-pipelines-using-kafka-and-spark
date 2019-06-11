package hbaseapplicationdevelopmentlifecycle.hbasedemo

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
    put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("column5"), Bytes.toBytes("value5"))
    put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("column6"), Bytes.toBytes("value6"))

    table.put(put)

    table.close
    connection.close()
  }
}
