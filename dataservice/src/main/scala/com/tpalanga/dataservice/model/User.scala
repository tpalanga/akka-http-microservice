package com.tpalanga.dataservice.model

object User {
  def fromNewUser(id: UserId, newUser: NewUser): User =
    User(id, newUser.name)
}
case class User(id: UserId, name: String)
