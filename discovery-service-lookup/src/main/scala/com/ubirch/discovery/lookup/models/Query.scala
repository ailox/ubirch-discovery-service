package com.ubirch.discovery.lookup.models

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.discovery.core.connector.GremlinConnector
import gremlin.scala.Key

import scala.language.postfixOps

object Query extends LazyLogging {

  val gc = GremlinConnector.get

  def getV(req: GetV): Unit = {
    req.message_type match {
      case "UPP" => query_UPP(req.ID)
    }
  }

  def query_UPP(hash: String): Unit = {

    gc.g.V().has(new Key[String]("IdAssigned"), hash).label().l().headOption match {
      case Some(value) => if (value != "upp") throw new IllegalArgumentException("message with id is not a UPP")
      case None => throw new IllegalArgumentException("message does not exist in the database")
    }

    val (passedSig, sigID) = retrieveIdFrom("signature", hash)
    if (passedSig) {
      val (passedFT, ftID) = retrieveIdFrom("foundation_tree", hash)

      if (passedFT) {
        val (passedRT, rtID) = retrieveIdFrom("root_tree", ftID)

        val blockChainVertices = gc.g.V().has(new Key[String]("IdAssigned"), rtID).bothE().bothV().hasLabel("blockchain_IOTA").l()
        blockChainVertices foreach { bcV =>
          val id = gc.g.V(bcV).values("IdAssigned").l().head.asInstanceOf[String]
          val label = gc.g.V(bcV).label().l().head
          logger.info(s"blockchain type: $label, id: $id")
        }
      }


    }


  }

  def retrieveIdFrom(name: String, hashUPP: String): (Boolean, String) = {
    val signatureID = gc.g.V().has(new Key[String]("IdAssigned"), hashUPP).bothE().bothV().hasLabel(name).values("IdAssigned").l().headOption.asInstanceOf[Option[String]]
    signatureID match {
      case Some(value) => logger.info(s"$name = $value")
        (true, value)
      case None => logger.info(s"$name = []")
        (false, "")
    }
  }

}
