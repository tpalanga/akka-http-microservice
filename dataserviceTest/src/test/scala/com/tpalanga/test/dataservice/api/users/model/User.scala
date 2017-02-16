package com.tpalanga.test.dataservice.api.users.model

import spray.json.{DefaultJsonProtocol, JsNull, JsValue, RootJsonFormat}

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
  implicit val newUserFormat: RootJsonFormat[NewUser] = jsonFormat1(NewUser)

  implicit object NoEntityFormat extends RootJsonFormat[NoEntity] {
    override def read(json: JsValue) = NoEntity()
    override def write(entity: NoEntity) = JsNull
  }
}

case class NoEntity()
case class User(id: String, name: String)
case class NewUser(name: String)
