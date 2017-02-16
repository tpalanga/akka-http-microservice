package com.tpalanga.dataservice.route

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.tpalanga.dataservice.model.UserDataStore

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class WebRoute(userService: ActorRef) extends SprayJsonSupport {
  import UserDataStore.DataFormats._

  implicit val askTimeout = Timeout(3.seconds)

  val route: Route =
    pathPrefix("data" / "users") {
      path(Segment) { id =>
        pathEnd {
          get {
            // get id
            onComplete((userService ? UserDataStore.GetOne(id)).mapTo[UserDataStore.GetUserResponse]) {
              case Success(oneUser: UserDataStore.OneUser) =>
                complete(oneUser.user)

              case Success(UserDataStore.NotFound(_)) =>
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
          entity(as[UserDataStore.User]) { user =>
            onComplete((userService ? UserDataStore.Update(user)).mapTo[UserDataStore.UpdateUserResponse]) {
              case Success(oneUser: UserDataStore.OneUser) =>
                complete(oneUser.user)

              case Success(UserDataStore.NotFound(_)) =>
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
          onComplete((userService ? UserDataStore.Delete(id)).mapTo[UserDataStore.Deleted]) {
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
          onComplete((userService ? UserDataStore.GetAll).mapTo[UserDataStore.AllUsers]) {
            case Success(allUsers) =>
              complete(allUsers.users)

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
          entity(as[UserDataStore.NewUser]) { user =>
            onComplete((userService ? UserDataStore.AddOne(user)).mapTo[UserDataStore.OneUser]) {
              case Success(oneUser) =>
                complete(StatusCodes.Created, oneUser.user)

              case Failure(th) =>
                extractLog { log =>
                  val msg = s"Creation of user $user failed"
                  log.error(th, msg)
                  complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
                }
            }
          }
        }
      }
    }
}
