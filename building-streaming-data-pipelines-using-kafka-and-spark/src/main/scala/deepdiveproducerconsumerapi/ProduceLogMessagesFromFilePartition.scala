package deepdiveproducerconsumerapi

import java.util.Properties
import java.io.File
import com.maxmind.geoip2.DatabaseReader
import java.net.InetAddress

import scala.io.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

/**
  * Created by itversity on 30/10/18.
  */
object ProduceLogMessagesFromFilePartition {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()

    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getString("bootstrap.server"))
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val inputDir = args(1)
    val topicName = args(2)

    val logMessages = Source.fromFile(inputDir).getLines.toList

    val database = new File("src/main/resources/db/maxmind/GeoLite2-Country.mmdb")
    val reader = new DatabaseReader.Builder(database).build

    logMessages.foreach(message => {
      try {
        val ipAddr = message.split(" ")(0)
        val countryIsoCode = reader.country(InetAddress.getByName(ipAddr)).getCountry.getIsoCode

        val partitionIndex = if (countryIsoCode == "US") 2
                            else countryIsoCode.hashCode() % 2
        println(countryIsoCode + " ==> " + partitionIndex)
        val record = new ProducerRecord[String, String](topicName, partitionIndex, ipAddr, message)
        producer.send(record)
      } catch {
        case e: Exception => {
          val record = new ProducerRecord[String, String](topicName + "_invalid", message)
          producer.send(record)
        }
      }
    })
    producer.close
  }
}
