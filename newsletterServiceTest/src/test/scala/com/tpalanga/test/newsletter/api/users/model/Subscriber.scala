package com.tpalanga.test.newsletter.api.users.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.collection.immutable.Seq

object SubscriberJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[Subscriber] = jsonFormat3(Subscriber)
  implicit val usersFormat: RootJsonFormat[Subscribers] = jsonFormat1(Subscribers)
}

case class Subscriber(id: String, name: String, email: String)
case class Subscribers(subscribers: Seq[Subscriber])