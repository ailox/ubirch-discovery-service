package com.ubirch.discovery.kafka

import java.util.concurrent.TimeUnit

import com.ubirch.discovery.kafka.consumer.DefaultExpressDiscoveryApp
import com.ubirch.util.PortGiver
import net.manub.embeddedkafka.EmbeddedKafkaConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.io.Source

object ExecuteSingleTest extends TestBase {

  val TEST_NAME = "/valid/inquiry/test2"

  def main(args: Array[String]): Unit = {
    implicit val config: EmbeddedKafkaConfig = EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = PortGiver.giveMeZookeeperPort)
    implicit val Deserializer: StringDeserializer = new StringDeserializer

    val msgToSend = readFile(getClass.getResource(TEST_NAME).getPath).mkString

    logger.info(msgToSend)

    withRunningKafka {

      val consumer = new DefaultExpressDiscoveryApp {}
      consumer.consumption.start()
      publishStringMessageToKafka("test", msgToSend)
      Thread.sleep(10000)
      logger.info("received: " + consumeFirstMessageFrom("com.ubirch.eventlog.discovery-error"))
      consumer.consumption.shutdown(300, TimeUnit.MILLISECONDS)
    }

  }

  def readFile(nameOfFile: String): List[String] = {
    val source = Source.fromFile(nameOfFile)
    val lines = source.getLines.toList
    source.close
    lines
  }

}
