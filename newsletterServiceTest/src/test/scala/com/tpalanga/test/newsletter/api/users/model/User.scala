package com.tpalanga.test.newsletter.api.users.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.collection.immutable.Seq

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val newUserFormat: RootJsonFormat[NewUser] = jsonFormat1(NewUser)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
  implicit val usersFormat: RootJsonFormat[Users] = jsonFormat1(Users)
}

case class NewUser(name: String)
case class User(id: String, name: String)
case class Users(users: Seq[User])