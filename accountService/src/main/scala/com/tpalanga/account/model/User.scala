package com.tpalanga.account.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object User {
  object DataFormats extends DefaultJsonProtocol {
    implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User.apply)
  }

  def fromNewUser(id: UserId, newUser: NewUser): User =
    User(id, newUser.name)
}

case class User(id: UserId, name: String)
