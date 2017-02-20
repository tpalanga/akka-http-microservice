package com.tpalanga.test.account.api.users.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.collection.immutable.Seq

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val newUserFormat: RootJsonFormat[NewUser] = jsonFormat2(NewUser)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
  implicit val usersFormat: RootJsonFormat[Users] = jsonFormat1(Users)
}

case class NewUser(name: String, email: String)
case class User(id: String, name: String, email: String)
case class Users(users: Seq[User])