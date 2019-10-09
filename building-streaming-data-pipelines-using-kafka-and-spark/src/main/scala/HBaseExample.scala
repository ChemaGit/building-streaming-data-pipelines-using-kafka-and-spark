import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql._
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, NamespaceDescriptor, TableName}
import org.apache.hadoop.hbase.client.{Admin, Connection, ConnectionFactory, Get, HBaseAdmin, Put, Table}
import org.apache.hadoop.hbase.util.Bytes

object HBaseExample {

  def getHbaseConnection(config: Config): Connection = {
    //Create Hbase Configuration Object
    val hBaseConf: Configuration = HBaseConfiguration.create()
    hBaseConf.set("hbase.zookeeper.quorum", config.getString("zookeeper.quorum"))
    //hBaseConf.set("hbase.zookeeper.property.clientPort", config.getString("zookeeper.port"))
    //hBaseConf.set("zookeeper.znode.parent","/hbase-unsecure")
    //hBaseConf.set("hbase.cluster.distributed","true")
    //Establish Connection
    val connection = ConnectionFactory.createConnection(hBaseConf)
    connection
  }

  def createTable(admin: Admin,nameSpace: String, tableName: String, familyName: String): Unit = {
    val namespace = NamespaceDescriptor.create(nameSpace).build()
    val tableDescriptor = new HTableDescriptor(s"$nameSpace:$tableName")
    val colDescriptor   = new HColumnDescriptor(familyName)
    tableDescriptor.addFamily(colDescriptor)
    if (!admin.tableExists(TableName.valueOf(tableName))) {
      admin.createNamespace(namespace)
      admin.createTable(tableDescriptor)
    }
  }
  //rowId, Date,Open,High,Low,Close,Volume
  def insertOrUpdateMetrics(rowId: String, date: String, open: String, high: String ,low: String,close:String,volume: String,envProps: Config): Unit = {
    //Hbase Metadata
    val columnFamily1 = "data"
    val connection = getHbaseConnection(envProps)

    val admin = connection.getAdmin
    if (admin.isTableAvailable(TableName.valueOf(Bytes.toBytes("demo:hbase")))) {
      val table = connection.getTable(TableName.valueOf("demo:hbase"))

      val row_get = new Get(Bytes.toBytes(rowId))
      //Insert Into Table
      val result = table.get(row_get)
      val value = result.getValue(Bytes.toBytes(columnFamily1),Bytes.toBytes(rowId))
      if(value == null) {
        val rowPut = new Put(Bytes.toBytes(rowId.toString))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("date"),Bytes.toBytes(date))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("open"),Bytes.toBytes(open))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("high"),Bytes.toBytes(high))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("low"),Bytes.toBytes(low))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("close"),Bytes.toBytes(close))
        rowPut.addColumn(Bytes.toBytes(columnFamily1),Bytes.toBytes("volume"),Bytes.toBytes(volume))
        table.put(rowPut)
      }
    }else {
      println("Table is not available, I don't know why")
    }
    connection.close()
  }


  def main(args: Array[String]): Unit = {
    val env = args(0)
    val conf = ConfigFactory.load
    val envProps: Config = conf.getConfig(args(0))

    val filtro = "Date"

    val spark = SparkSession
      .builder()
      .appName("HBase Example")
      .master("local[*]")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    val broadcastConfig = sc.broadcast(envProps)

    createTable(getHbaseConnection(broadcastConfig.value).getAdmin,"demo","hbase","data")

    val data = sc.textFile("src/main/resources/db/GOOG-NYSE_PFE.csv")
      .map(line => line.split(",")).filter(arr => arr(0) != filtro)
    val headers = data.first()
    //data.take(10).foreach(x => println(x.mkString(",")))
    //println(headers.mkString(","))

    var rowId = 0
    data.foreach(lines =>{
      //Date,Open,High,Low,Close,Volume
      val date = lines(0)
      val open = lines(1)
      val high = lines(2)
      val low = lines(3)
      val close = lines(4)
      val volume = lines(5)
      rowId += 1
      //println(s"${rowId} $date  $open  $high  $low  $close  $volume")
      insertOrUpdateMetrics(rowId.toString, date,open,high,low,close,volume, broadcastConfig.value)
    })

    sc.stop()
    spark.stop()
  }

}