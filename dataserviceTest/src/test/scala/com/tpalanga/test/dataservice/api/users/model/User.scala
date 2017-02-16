package com.tpalanga.test.dataservice.api.users.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
  implicit val newUserFormat: RootJsonFormat[NewUser] = jsonFormat1(NewUser)
}

case class User(id: String, name: String)
case class NewUser(name: String)

