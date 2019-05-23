import java.util.Properties
import java.io.File
import com.maxmind.geoip2.DatabaseReader
import java.net.InetAddress

import scala.io.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object ProducerFromFile {
  def main(args: Array[String]): Unit = {
    val props = new Properties
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "quickstart.cloudera:9092")
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    import scala.io.Source
    val logMessages = Source.fromFile("/opt/gen_logs/logs/access.log").getLines.toList

    logMessages.foreach(message => {
      val ipAddr = message.split(" ")(0)
      println(message)
      val record = new ProducerRecord("retail_multi", ipAddr, message)
      producer.send(record)
    })
  }
}

