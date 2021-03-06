package deepdiveproducerconsumerapi

import java.util.{Collections, Properties}

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}

import scala.collection.JavaConversions._

object ConsumerDevice {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getString("bootstrap.server"))
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "KafkaConsumerExample")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    //props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "1")

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(Collections.singletonList("device"))
    while(true){
      val records = consumer.poll(500)
      println("Num records => " + records.count())
      if(!records.iterator().isEmpty)  println(records.last.value())
      //for (record <- records.iterator()) {
        //println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset())
      //}
    }
  }
}
