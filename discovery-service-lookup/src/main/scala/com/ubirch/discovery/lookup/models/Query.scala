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

    val signatureID = gc.g.V().has(new Key[String]("IdAssigned"), hash).bothE().bothV().hasLabel("signature").values("IdAssigned").l().head.asInstanceOf[String]
    logger.info(s"signature ID = $signatureID")
    val ftID = gc.g.V().has(new Key[String]("IdAssigned"), hash).bothE().bothV().hasLabel("foundation_tree").values("IdAssigned").l().head.asInstanceOf[String]
    logger.info(s"foundation tree ID = $ftID")
    val rtID = gc.g.V().has(new Key[String]("IdAssigned"), ftID).bothE().bothV().hasLabel("root_tree").values("IdAssigned").l().head.asInstanceOf[String]
    logger.info(s"root tree ID = $rtID")
    val blockChainVertices = gc.g.V().has(new Key[String]("IdAssigned"), rtID).bothE().bothV().hasLabel("blockchain_IOTA").l()
    blockChainVertices foreach { bcV =>
      val id = gc.g.V(bcV).values("IdAssigned").l().head.asInstanceOf[String]
      val label = gc.g.V(bcV).label().l().head
      logger.info(s"blockchain type: $label, id: $id")
    }

  }

}
