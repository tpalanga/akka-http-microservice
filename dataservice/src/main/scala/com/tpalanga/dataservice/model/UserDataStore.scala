package com.tpalanga.dataservice.model

import akka.actor.{Actor, Props}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.collection.immutable.Seq

object UserDataStore {

  sealed trait Request
  case object GetAll extends Request
  case class GetOne(id: UserId) extends Request
  case class AddOne(user: NewUser) extends Request
  case class Update(user: User) extends Request
  case class Delete(id: UserId) extends Request

  case class AllUsers(users: Seq[User])

  sealed trait GetUserResponse
  sealed trait UpdateUserResponse
  sealed trait DeleteUserResponse
  case class Deleted(id: UserId) extends DeleteUserResponse
  case class OneUser(user: User) extends GetUserResponse with UpdateUserResponse
  case class NotFound(id: UserId) extends GetUserResponse with UpdateUserResponse with DeleteUserResponse

  object DataFormats extends DefaultJsonProtocol {
    implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User.apply)
    implicit val newUserFormat: RootJsonFormat[NewUser] = jsonFormat1(NewUser)
    implicit val oneUserFormat: RootJsonFormat[OneUser] = jsonFormat1(OneUser)
    implicit val allUsersFormat: RootJsonFormat[AllUsers] = jsonFormat1(AllUsers)
  }

  def props() = Props(new UserDataStore)

  def newUUID(): String = java.util.UUID.randomUUID.toString
}

class UserDataStore extends Actor {
  import UserDataStore._

  var users: Map[UserId, User] = Map.empty

  override def receive: Receive = {
    case GetAll =>
      sender() ! AllUsers(users.values.to[Seq])

    case GetOne(id) =>
      sender() ! users.get(id).map(OneUser).getOrElse(NotFound(id))

    case AddOne(newUser) =>
      val user = User.fromNewUser(newUUID(), newUser)
      users = users + (user.id -> user)
      sender() ! OneUser(user)

    case Update(user) =>
      val reply = users.get(user.id).map { _ =>
        users = users + (user.id -> user)
        OneUser(user)
      }.getOrElse(NotFound(user.id))
      sender() ! reply

    case Delete(id) =>
      val reply = users.get(id).map { _ =>
        users = users - id
        Deleted(id)
      }.getOrElse(NotFound(id))
      sender() ! reply
  }
}
