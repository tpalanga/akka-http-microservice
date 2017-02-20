package com.tpalanga.newsletter.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object Subscriber {
  object DataFormats extends DefaultJsonProtocol {
    implicit val subscriberFormat: RootJsonFormat[Subscriber] = jsonFormat3(Subscriber.apply)
  }
}
case class Subscriber(id: UserId, name: String, email: String)
