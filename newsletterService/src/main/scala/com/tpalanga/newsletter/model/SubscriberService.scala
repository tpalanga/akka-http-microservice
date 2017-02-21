package com.tpalanga.newsletter.model

import akka.actor.{Actor, ActorLogging, Props}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.collection.immutable.Seq

object SubscriberService {

  sealed trait Request
  case object GetAll extends Request
  case class GetOne(id: UserId) extends Request
  case class AddOne(subscriber: Subscriber) extends Request
  case class Update(subscriber: Subscriber) extends Request
  case class Delete(id: UserId) extends Request

  sealed trait GetSubscriberResponse
  sealed trait AddSubscriberResponse
  sealed trait UpdateSubscriberResponse
  sealed trait DeleteSubscriberResponse
  sealed trait GetAllSubscribersResponse
  case class Deleted(id: UserId) extends DeleteSubscriberResponse
  case class OneSubscriber(subscriber: Subscriber) extends GetSubscriberResponse with AddSubscriberResponse with UpdateSubscriberResponse
  case class NotFound(id: UserId) extends GetSubscriberResponse with UpdateSubscriberResponse with DeleteSubscriberResponse
  case object AlreadyExists extends AddSubscriberResponse
  case class AllSubscribers(subscribers: Seq[Subscriber]) extends GetAllSubscribersResponse

  object DataFormats extends DefaultJsonProtocol {
    import Subscriber.DataFormats._
    implicit val oneSubscriberFormat: RootJsonFormat[OneSubscriber] = jsonFormat1(OneSubscriber)
    implicit val allSubscriberFormat: RootJsonFormat[AllSubscribers] = jsonFormat1(AllSubscribers)
  }

  def props() = Props(new SubscriberService)
}

class SubscriberService extends Actor with ActorLogging {
  import SubscriberService._

  private var subscribers: Map[UserId, Subscriber] = Map.empty

  override def receive: Receive = {
    case GetAll =>
      sender() ! AllSubscribers(subscribers.values.to[Seq])

    case GetOne(id) =>
      sender() ! subscribers.get(id).map(OneSubscriber).getOrElse(NotFound(id))

    case AddOne(subscriber) =>
      val reply = subscribers.values.find(_.id == subscriber.id)
        .map(_ => AlreadyExists)
        .getOrElse {
          subscribers = subscribers + (subscriber.id -> subscriber)
          OneSubscriber(subscriber)
        }
      sender() ! reply

    case Update(subscriber) =>
      val reply = subscribers.get(subscriber.id).map { _ =>
        subscribers = subscribers + (subscriber.id -> subscriber)
        OneSubscriber(subscriber)
      }.getOrElse(NotFound(subscriber.id))
      sender() ! reply

    case Delete(id) =>
      val reply = subscribers.get(id).map { _ =>
        subscribers = subscribers - id
        Deleted(id)
      }.getOrElse(NotFound(id))
      sender() ! reply
  }

  override def unhandled(msg: Any) {
    log.warning(s"Unhandled message $msg")
    super.unhandled(msg)
  }
}
