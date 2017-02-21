package com.tpalanga.newsletter.route

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, ValidationRejection}
import akka.pattern.ask
import akka.util.Timeout
import com.tpalanga.newsletter.model.{Subscriber, SubscriberService}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class SubscriberRoute(subscriberService: ActorRef) extends SprayJsonSupport {
  import Subscriber.DataFormats._
  import SubscriberService.DataFormats._

  implicit val askTimeout = Timeout(3.seconds)

  val route: Route =
    pathPrefix("data" / "subscribers") {
      path(Segment) { id =>
        pathEnd {
          get {
            // get id
            onComplete((subscriberService ? SubscriberService.GetOne(id)).mapTo[SubscriberService.GetSubscriberResponse]) {
              case Success(oneSubscriber: SubscriberService.OneSubscriber) =>
                complete(oneSubscriber.subscriber)

              case Success(SubscriberService.NotFound(_)) =>
                complete(StatusCodes.NotFound, s"Subscriber with ID $id not found")

              case Failure(th) =>
                extractLog { log =>
                  val msg = s"Getting subscriber with ID $id from subscriberService failed"
                  log.error(th, msg)
                  complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
                }
            }
          }
        } ~
        put {
          // update
          entity(as[Subscriber]) { subscriber =>
            onComplete((subscriberService ? SubscriberService.Update(subscriber)).mapTo[SubscriberService.UpdateSubscriberResponse]) {
              case Success(oneSubscriber: SubscriberService.OneSubscriber) =>
                complete(oneSubscriber.subscriber)

              case Success(SubscriberService.NotFound(_)) =>
                complete(StatusCodes.NotFound, s"Subscriber with ID $id not found")

              case Failure(th) =>
                extractLog { log =>
                  val msg = s"Updating subscriber $subscriber failed"
                  log.error(th, msg)
                  complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
                }
            }
          }

        } ~
        delete {
          // delete
          onComplete((subscriberService ? SubscriberService.Delete(id)).mapTo[SubscriberService.DeleteSubscriberResponse]) {
            case Success(_) =>
              complete("Deleted")

            case Failure(th) =>
              extractLog { log =>
                val msg = s"Deleting subscriber with ID $id failed"
                log.error(th, msg)
                complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
              }
          }
        }
      } ~
      pathEnd {
        get {
          // list
          onComplete((subscriberService ? SubscriberService.GetAll).mapTo[SubscriberService.GetAllSubscribersResponse]) {
            case Success(allSubscribers: SubscriberService.AllSubscribers) =>
              complete(allSubscribers)

            case Failure(th) =>
              extractLog { log =>
                val msg = "Getting all subscribers from subscriberService failed"
                log.error(th, msg)
                complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
              }
          }
        } ~
        post {
          // create
          entity(as[Subscriber]) { subscriber =>
            onComplete((subscriberService ? SubscriberService.AddOne(subscriber)).mapTo[SubscriberService.AddSubscriberResponse]) {
              case Success(oneSubscriber: SubscriberService.OneSubscriber) =>
                complete(StatusCodes.Created, oneSubscriber.subscriber)

              case Success(SubscriberService.AlreadyExists) =>
                reject(ValidationRejection("Subscriber already exists"))

              case Failure(th) =>
                extractLog { log =>
                  val msg = s"Creation of subscriber with ID '${subscriber.id}' failed"
                  log.error(th, msg)
                  complete(StatusCodes.InternalServerError, s"$msg: ${th.getMessage}")
                }
            }
          }
        }
      }
    }
}
