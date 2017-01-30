package com.tpalanga.dataservice.model

import akka.actor.Actor

import scala.collection.immutable.Seq

object UserDataStore {
  type UserId = String
  case class User(id: UserId, name: String)

  sealed trait Request
  case object GetAll extends Request
  case class GetOne(id: UserId) extends Request
  case class AddOne(user: User) extends Request

  sealed trait Response
  case class All(users: Seq[User])
  case class One(user: User)
  case class NotFound(id: UserId)

}

class UserDataStore extends Actor {
  import UserDataStore._

  var users: Map[UserId, User] = Map.empty

  override def receive: Receive = {
    case GetAll =>
      sender() ! All(users.values.to[Seq])

    case GetOne(id) =>
      sender() ! users.getOrElse(id, NotFound(id))

    case AddOne(user) =>
      users = users + (user.id -> user)
      sender() ! One(user)
  }
}
