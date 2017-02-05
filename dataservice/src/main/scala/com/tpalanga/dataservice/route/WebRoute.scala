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
            onComplete((userService ? UserDataStore.GetOne(id)).mapTo[UserDataStore.OneUserResponse]) {
              case Success(oneUser: UserDataStore.OneUser) =>
                complete(oneUser)

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
        }
      } ~
      pathEnd {
        get {
          // list
          onComplete((userService ? UserDataStore.GetAll).mapTo[UserDataStore.AllUsers]) {
            case Success(allUsers) =>
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
          entity(as[UserDataStore.User]) { user =>
            onComplete((userService ? UserDataStore.AddOne(user)).mapTo[UserDataStore.OneUser]) {
              case Success(_) =>
                complete(StatusCodes.Created)

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
