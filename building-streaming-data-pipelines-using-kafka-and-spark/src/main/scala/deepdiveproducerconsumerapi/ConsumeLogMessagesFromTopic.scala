package deepdiveproducerconsumerapi

import java.util.{Collections, Properties}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import scala.collection.JavaConversions._

object ConsumeLogMessagesFromTopic {

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val envProps = conf.getConfig(args(0))
    val props = new Properties()

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,envProps.getString("bootstrap.server"))
    props.put(ConsumerConfig.CLIENT_ID_CONFIG,"Consume messages from topic")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer")

    props.put(ConsumerConfig.GROUP_ID_CONFIG, "cgb3")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

    val consumer = new KafkaConsumer[String, String](props)
    //val partitions = Set(new TopicPartition(args(1), args(2).toInt))
    val partitions = Set(new TopicPartition(args(1), args(2).toInt))
    consumer.assign(partitions)
    consumer.seekToBeginning(consumer.assignment())

    while(true) {
      val records = consumer.poll(500)
      println("Num records => " + records.count())
      for(record <- records) {
        println("Received message: (" + record.key()
          + ", " + record.value()
          + ") from " + record.partition()
          + " at offset " + record.offset())
      }
      Thread.sleep(100)
    }
  }
}

