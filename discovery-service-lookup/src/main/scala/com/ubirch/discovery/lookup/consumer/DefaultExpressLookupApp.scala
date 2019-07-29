package com.ubirch.discovery.lookup.consumer

import com.ubirch.discovery.lookup.models.{GetV, Query}
import com.ubirch.discovery.lookup.util.ErrorsHandler
import com.ubirch.discovery.lookup.util.Exceptions.{ParsingException, StoreException}
import com.ubirch.kafka.express.ExpressKafkaApp
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer, StringSerializer}
import org.json4s._

import scala.language.postfixOps
import scala.util.Try

trait DefaultExpressLookupApp extends ExpressKafkaApp[String, String] {

  override val producerBootstrapServers: String = conf.getString("kafkaApi.kafkaProducer.bootstrapServers")

  override val keySerializer: serialization.Serializer[String] = new StringSerializer

  override val valueSerializer: serialization.Serializer[String] = new StringSerializer

  override val consumerTopics: Set[String] = conf.getString("kafkaApi.kafkaConsumer.topic").split(", ").toSet

  val producerTopic: String = "com.ubirch.eventlog.newlookup.prod" //TODO: change on final version

  val producerErrorTopic: String = conf.getString("kafkaApi.kafkaConsumer.errorTopic")

  override val consumerBootstrapServers: String = conf.getString("kafkaApi.kafkaConsumer.bootstrapServers")

  override val consumerGroupId: String = conf.getString("kafkaApi.kafkaConsumer.groupId")

  override val consumerMaxPollRecords: Int = conf.getInt("kafkaApi.kafkaConsumer.maxPoolRecords")

  override val consumerGracefulTimeout: Int = conf.getInt("kafkaApi.kafkaConsumer.gracefulTimeout")

  override val keyDeserializer: Deserializer[String] = new StringDeserializer

  override val valueDeserializer: Deserializer[String] = new StringDeserializer

  override def process(consumerRecords: Vector[ConsumerRecord[String, String]]): Unit = {
    consumerRecords.foreach { cr =>

      logger.debug("Received value: " + cr.value())

      Try(parseRelations(cr.value())).map(query).recover {
        case e: ParsingException =>
          send(producerErrorTopic, ErrorsHandler.generateException(e))
          logger.error(ErrorsHandler.generateException(e))
        case e: StoreException =>
          send(producerErrorTopic, ErrorsHandler.generateException(e))
          logger.error(ErrorsHandler.generateException(e))
      }

    }
  }

  def parseRelations(data: String): Seq[GetV] = {
    implicit val formats: DefaultFormats = DefaultFormats
    data match {
      case "" => throw ParsingException(s"Error parsing data [received empty message: $data]")
      case "[]" => throw ParsingException(s"Error parsing data [received empty message: $data]")
      case _ =>
    }
    try {
      jackson.parseJson(data).extract[Seq[GetV]]
    } catch {
      case e: Exception =>
        throw ParsingException(s"Error parsing data [${e.getMessage}]")
    }
  }

  def query(data: Seq[GetV]): Boolean = {
    try {
      val t0 = System.currentTimeMillis()
      data.foreach(gv => send(producerTopic, Query.getV(gv)))
      logger.info("Time for querying: " + (System.currentTimeMillis() - t0).toString)
      true
    } catch {
      case e: Exception =>
        logger.error("Error storing graph: " + e.getMessage)
        throw StoreException("Error storing graph: " + e.getMessage)
    }
  }

}
