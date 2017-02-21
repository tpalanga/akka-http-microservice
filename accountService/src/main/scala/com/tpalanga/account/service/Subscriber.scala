package com.tpalanga.account.service

import com.tpalanga.account.model.User
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object SubscriberJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[Subscriber] = jsonFormat3(Subscriber.apply)

}

object Subscriber {
  def apply(user: User): Subscriber = Subscriber(user.id, user.name, user.email)
}

case class Subscriber(id: String, name: String, email: String)