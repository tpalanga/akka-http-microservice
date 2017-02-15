package com.tpalanga.test.dataservice.api.users.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
}

case class User(id: String, name: String)

