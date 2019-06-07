package com.ubirch.discovery.core.structure

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.discovery.core.connector.GremlinConnector
import com.ubirch.discovery.core.util.Util._
import gremlin.scala._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }
import org.scalatest.{ FeatureSpec, Matchers }

class VertexStructDbSpec extends FeatureSpec with Matchers with LazyLogging {

  implicit val gc: GremlinConnector = GremlinConnector.get

  private val dateTimeFormat = ISODateTimeFormat.dateTime()

  val Number: Key[String] = Key[String]("number")
  val Name: Key[String] = Key[String]("name")
  val Created: Key[String] = Key[String]("created")
  val test: Key[String] = Key[String]("truc")
  val IdAssigned: Key[String] = Key[String]("IdAssigned")

  def deleteDatabase(): Unit = {
    gc.g.V().drop().iterate()
  }

  feature("generate a vertex") {

    scenario("test") {
      deleteDatabase()

      val theId = 1

      val vSDb = new VertexStructDb(theId.toString, gc.g)
      val now = DateTime.now(DateTimeZone.UTC)
      val properties: List[KeyValue[String]] = List(
        new KeyValue[String](Number, "5"),
        new KeyValue[String](Name, "aName"),
        new KeyValue[String](Created, dateTimeFormat.print(now))
      )
      vSDb.addVertex(properties, "aLabel", gc.b)

      val response = vSDb.getPropertiesMap
      val label = vSDb.vertex.label
      logger.debug(response.mkString)
      logger.debug(label)

      val propertiesKey = Array(Number, Name, Created, IdAssigned)

      val idGottenBack = extractValue[String](response, "IdAssigned")
      val propertiesReceived = recompose(response, propertiesKey)

      propertiesReceived.sortBy(x => x.key.name) shouldBe properties.sortBy(x => x.key.name)
      idGottenBack shouldBe theId.toString
    }

  }

}

