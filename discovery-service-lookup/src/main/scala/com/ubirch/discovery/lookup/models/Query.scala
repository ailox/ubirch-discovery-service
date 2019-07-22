package com.ubirch.discovery.lookup.models

import com.typesafe.scalalogging.LazyLogging
import gremlin.scala.Key

import scala.language.postfixOps

object Query extends LazyLogging {

  val gc = GremlinConnector.get
  val DEFAULT_EMPTY_STRING = "[]"

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
    val (passedFT, ftID) = if (passedSig) retrieveIdFrom("foundation_tree", hash) else (false, DEFAULT_EMPTY_STRING)
    val (passedRT, rtID) = if (passedFT) retrieveIdFrom("root_tree", ftID) else (false, DEFAULT_EMPTY_STRING)
    val blockChains: List[(String, String)] = if (passedRT) {
      val blockChainVertices = gc.g.V().has(new Key[String]("IdAssigned"), rtID).bothE().bothV().hasLabel("blockchain_IOTA").l()
      blockChainVertices map { bcV =>
        val id = gc.g.V(bcV).values("IdAssigned").l().head.asInstanceOf[String]
        val label = gc.g.V(bcV).label().l().head
        (label, id)
      }
    } else {
      List(("BlockChains", "[]"))
    }

    val toPrint = List(("signature", sigID), ("foundation tree", ftID), ("root tree", rtID)) ++ blockChains

    toPrint foreach { kv =>
      logger.info(s"${kv._1} = ${kv._2}")
    }

  }

  def retrieveIdFrom(name: String, hashUPP: String): (Boolean, String) = {
    val vertexId = gc.g.V().has(new Key[String]("IdAssigned"), hashUPP).bothE().bothV().hasLabel(name).values("IdAssigned").l().headOption.asInstanceOf[Option[String]]
    vertexId match {
      case Some(value) => (true, value)
      case None => (false, "")
    }
  }

}
