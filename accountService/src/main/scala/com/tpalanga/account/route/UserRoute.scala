package com.tpalanga.account.route

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, ValidationRejection}
import akka.pattern.ask
import akka.util.Timeout
import com.tpalanga.account.model.{NewUser, User}
import com.tpalanga.account.service.UserService

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class UserRoute(userService: ActorRef) extends SprayJsonSupport {
  import User.DataFormats._
  import UserService.DataFormats._

  implicit val askTimeout = Timeout(3.seconds)

  val route: Route =
    pathPrefix("data" / "users") {
      path(Segment) { id =>
        pathEnd {
          get {
            // get id
            onComplete((userService ? UserService.GetOne(id)).mapTo[UserService.GetUserResponse]) {
              case Success(oneUser: UserService.OneUser) =>
                complete(oneUser.user)

              case Success(UserService.NotFound(_)) =>
                complete(StatusCodes.NotFound, s"User with ID $id not found")

              case Failure(th) =>
                extractLog { log =>
                  val msg = s"Getting user with ID $id from userService failed"
                  log.error(th, msg)
                  complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
                }
            }
          }
        } ~
        put {
          // update
          entity(as[User]) { user =>
            onComplete((userService ? UserService.Update(user)).mapTo[UserService.UpdateUserResponse]) {
              case Success(oneUser: UserService.OneUser) =>
                complete(oneUser.user)

              case Success(UserService.NotFound(_)) =>
                complete(StatusCodes.NotFound, s"User with ID $id not found")

              case Failure(th) =>
                extractLog { log =>
                  val msg = s"Updating user $user failed"
                  log.error(th, msg)
                  complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
                }
            }
          }

        } ~
        delete {
          // delete
          onComplete((userService ? UserService.Delete(id)).mapTo[UserService.DeleteUserResponse]) {
            case Success(_) =>
              complete("Deleted")

            case Failure(th) =>
              extractLog { log =>
                val msg = s"Deleting user with ID $id failed"
                log.error(th, msg)
                complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
              }
          }
        }
      } ~
      pathEnd {
        get {
          // list
          onComplete((userService ? UserService.GetAll).mapTo[UserService.GetAllUserResponse]) {
            case Success(allUsers: UserService.AllUsers) =>
              complete(allUsers)

            case Failure(th) =>
              extractLog { log =>
                val msg = "Getting all users from userService failed"
                log.error(th, msg)
                complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
              }
          }
        } ~
        post {
          // create
          entity(as[NewUser]) { user =>
            onComplete((userService ? UserService.AddOne(user)).mapTo[UserService.AddUserResponse]) {
              case Success(oneUser: UserService.OneUser) =>
                complete(StatusCodes.Created, oneUser.user)

              case Success(UserService.AlreadyExists) =>
                reject(ValidationRejection("User already exists"))

              case Failure(th) =>
                extractLog { log =>
                  val msg = s"Creation of user '${user.name}' failed"
                  log.error(th, msg)
                  complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
                }
            }
          }
        }
      }
    }
}
