package com.tpalanga.account.service

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, RequestEntity}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import com.tpalanga.testlib.test.client.{NoEntity, Response, RestServiceClient}
import com.tpalanga.testlib.test.config.RestServiceConfig

import scala.concurrent.{ExecutionContext, Future}

object NewsletterClient {
  type NewsletterClientFactory = (RestServiceConfig, ActorSystem) => NewsletterClient

  def defaultFactory: NewsletterClientFactory =
    (config, system) => new NewsletterClient(config, system)
}

class NewsletterClient(val restServiceConfig: RestServiceConfig, val system: ActorSystem) extends RestServiceClient {
  import NoEntity.DataFormats._
  import SubscriberJsonProtocol._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  private implicit val _system = system
  private implicit val materializer: Materializer = ActorMaterializer(ActorMaterializerSettings(system))

  def subscriberRetrieve(id: String)(implicit ec: ExecutionContext): Future[Response[Subscriber]] =
    client.get(s"/data/subscribers/$id").map { httpResponse =>
      Response[Subscriber](httpResponse)
    }

  def subscriberCreate(subscriber: Subscriber)(implicit ec: ExecutionContext): Future[Response[Subscriber]] =
    for {
      entity <- Marshal(subscriber).to[RequestEntity]
      httpResponse <- client.post(s"/data/subscribers", Nil, entity.withContentType(ContentTypes.`application/json`))
    } yield Response[Subscriber](httpResponse)

  def subscriberDelete(id: String)(implicit ec: ExecutionContext): Future[Response[NoEntity]] =
    client.delete(s"/data/subscribers/$id").map { httpResponse =>
      Response[NoEntity](httpResponse)
    }

}
