package com.tpalanga.account.service

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.http.scaladsl.model.StatusCodes
import com.tpalanga.account.model.{User, UserId}
import com.tpalanga.account.service.NewsletterClient.NewsletterClientFactory
import com.tpalanga.testlib.test.client.{NoEntity, Response}
import com.tpalanga.testlib.test.config.RestServiceConfig

object NewsletterService {
  case class Subscribe(user: User)
  case class Unsubscribe(id: UserId)

  case class CreateResponse(response: Response[Subscriber])
  case class DeleteResponse(response: Response[NoEntity])

  def props(restServiceConfig: RestServiceConfig, clientFactory: NewsletterClientFactory = NewsletterClient.defaultFactory): Props =
    Props(new NewsletterService(restServiceConfig, clientFactory))
}

class NewsletterService(restServiceConfig: RestServiceConfig, clientFactory: NewsletterClientFactory) extends Actor with ActorLogging {
  import NewsletterService._
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case Subscribe(user) =>
      newClient().subscriberCreate(Subscriber(user)).map(CreateResponse) pipeTo self

    case Unsubscribe(userId) =>
      newClient().subscriberDelete(userId).map(DeleteResponse) pipeTo self

    case CreateResponse(response) if response.status == StatusCodes.Created =>
      log.info("Subscribed to newsletter")

    case CreateResponse(response) =>
      log.info(s"Unexpected response while subscribing to newsletter $response")

    case DeleteResponse(response) if response.status == StatusCodes.OK =>
      log.info("Unsubscribed from newsletter")

    case DeleteResponse(response) =>
      log.info("Unsubscribed from newsletter")

    case Status.Failure(th) =>
      log.error(th, "Error on newsletter request")
  }

  private def newClient() = clientFactory(restServiceConfig, context.system)
}
