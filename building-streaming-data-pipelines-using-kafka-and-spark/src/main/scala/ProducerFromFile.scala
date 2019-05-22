import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import scala.io.Source

object ProducerFromFile {
  def main(args: Array[String]): Unit = {
    val props = new Properties
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "quickstart.cloudera:9092")
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "Produce log messages from file")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[Nothing, String](props)

    import scala.io.Source
    val logMessages = Source.fromFile("/opt/gen_logs/logs/access.log").getLines.toList

    logMessages.foreach(message => {
      val record = new ProducerRecord("retail_multi", message)
      println(record)
      producer.send(record)
    })
  }
}
