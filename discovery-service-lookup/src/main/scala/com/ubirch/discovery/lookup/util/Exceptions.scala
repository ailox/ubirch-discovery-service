package com.ubirch.discovery.lookup.util

object Exceptions {

  case class ParsingException(message: String) extends Exception(message)

  case class StoreException(message: String) extends Exception(message)

}
