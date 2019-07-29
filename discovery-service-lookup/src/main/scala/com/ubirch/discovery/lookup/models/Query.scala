package com.ubirch.discovery.lookup.models

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.discovery.core.connector.GremlinConnector
import com.ubirch.discovery.lookup.util.Util
import gremlin.scala.Key

import scala.language.postfixOps

object Query extends LazyLogging {

  val gc: GremlinConnector = GremlinConnector.get
  val DEFAULT_EMPTY_STRING = "[]"

  def getV(req: GetV): String = {
    req.message_type match {
      case "upp" => query_UPP(req.ID)
    }
  }

  def query_UPP(hash: String): String = {

    gc.g.V().has(new Key[String]("signature"), hash).label().l().headOption match {
      case Some(value) => if (value != "upp") throw new IllegalArgumentException("message with id is not a UPP")
      case None => throw new IllegalArgumentException("message does not exist in the database")
    }

    val (passedSig, sigID) = getSignatureFromHash(hash)
    val (passedFT, ftID) = if (passedSig) retrieveIdFrom("foundation_tree", hash, "signature", "hash") else (false, DEFAULT_EMPTY_STRING)
    val (passedRT, rtID) = if (passedFT) retrieveIdFrom("root_tree", ftID, "hash", "hash") else (false, DEFAULT_EMPTY_STRING)
    val blockChains: List[(String, String)] = if (passedRT) {
      val blockChainVertices = gc.g.V().has(new Key[String]("hash"), rtID).bothE().bothV().hasLabel("blockchain").l()
      blockChainVertices map { bcV =>
        val hash = gc.g.V(bcV).values("hash").l().head.asInstanceOf[String]
        val bcType = gc.g.V(bcV).values("type").l().head.asInstanceOf[String]
        (bcType, hash)
      }
    } else {
      List(("BlockChains", "[]"))
    }

    val toPrint = List(("signature", sigID), ("foundation tree", ftID), ("root tree", rtID)) ++ blockChains

    logger.info("fullpath: " + Util.toJson(toPrint))
    Util.toJson(toPrint)
  }

  // TODO: change once final thingy put in place
  def getSignatureFromHash(hash: String): (Boolean, String) = {
    (true, hash)
  }

  def retrieveIdFrom(name: String, value: String, key: String, keyOfValueToGet: String): (Boolean, String) = {
    val vertexId = gc.g.V().has(new Key[String](key), value).bothE().bothV().hasLabel(name).values(keyOfValueToGet).l().headOption.asInstanceOf[Option[String]]
    vertexId match {
      case Some(something) => (true, something)
      case None => (false, "")
    }
  }

}
