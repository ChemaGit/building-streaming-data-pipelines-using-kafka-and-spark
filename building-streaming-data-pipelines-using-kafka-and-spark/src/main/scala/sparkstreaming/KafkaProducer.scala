package sparkstreaming

import scala.io.StdIn._
import com.typesafe.config.ConfigFactory
import java.util.Properties
import org.apache.kafka.clients.producer._

object KafkaProducer {
	def main(args: Array[String]): Unit = {
		val conf = ConfigFactory.load
	  val envConfig = conf.getConfig(args(0))

		val props = new Properties()
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envConfig.getString("bootstrap.server"))
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducerExample")
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
		
		val kafkaProducer = new KafkaProducer[String, String](props)

		val record = new ProducerRecord[String, String](envConfig.getString("topic.test"),"Hello", "World")
		kafkaProducer.send(record)

		var mess = readFromConsole()
		do {
			val rec: ProducerRecord[String, String] = new ProducerRecord[String, String](envConfig.getString("topic.test"),mess, mess)
			kafkaProducer.send(rec)
			mess = readFromConsole()
		}while(mess != "quit")


		kafkaProducer.close()
	}

	def readFromConsole(): String = {
		println("""Some message until "quit": """)
		val message = readLine
		message
	}

}
