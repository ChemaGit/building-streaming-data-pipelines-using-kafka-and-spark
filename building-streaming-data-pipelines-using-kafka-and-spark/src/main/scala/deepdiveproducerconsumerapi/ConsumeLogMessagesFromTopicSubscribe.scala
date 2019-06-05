package deepdiveproducerconsumerapi

import java.util.{Properties, Collections}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import scala.collection.JavaConversions._

object ConsumeLogMessagesFromTopicSubscribe {

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getString("bootstrap.server"))
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "Consume Messages from Topic")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "dg30")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    // props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
    //  "range")

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(Collections.singletonList(args(1)))

    while(true) {
      val records = consumer.poll(500)
      for(record <- records) {
        println("Received message: (" + record.key()
          + ", " + record.value()
          + ") from " + record.partition()
          + " at offset " + record.offset())
      }
    }
  }

}
