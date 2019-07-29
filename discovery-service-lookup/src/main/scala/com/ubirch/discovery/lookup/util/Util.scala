package com.ubirch.discovery.lookup.util

import com.typesafe.scalalogging.LazyLogging
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}


object Util extends LazyLogging {

  def toJson(message: List[(String, String)]): String = {

    implicit val formats: Formats = DefaultFormats
    val fullPath = message map { n => n._1 -> n._2 }
    Serialization.write(fullPath)

  }

  case class Node(name: String, value: String)

  case class FullPath(res: List[Node])

}
