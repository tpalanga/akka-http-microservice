package com.tpalanga.test.newsletter.api.subscriber

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, RequestEntity}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import com.tpalanga.test.config.TestConfig
import com.tpalanga.test.newsletter.api.subscriber.model.{Subscriber, Subscribers}
import com.tpalanga.testlib.test.client.{NoEntity, Response, RestServiceClient}
import com.tpalanga.testlib.test.config.RestServiceConfig

import scala.concurrent.{ExecutionContext, Future}

// TODO (TP): extract to common library and add implementation class
trait NewsletterServiceRestServiceClient extends RestServiceClient {
  import NoEntity.DataFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.tpalanga.test.newsletter.api.subscriber.model.SubscriberJsonProtocol._

  val testConfig: TestConfig

  override val restServiceConfig: RestServiceConfig = testConfig.newsletterServiceConfig
  println(s"NewsletterServiceRestServiceClient: $restServiceConfig")
  private implicit val materializer: Materializer = ActorMaterializer(ActorMaterializerSettings(system))

  private def logResponse[T](response: Response[T]) = {
    // TODO (TP): use a logger
    //println(response)
    response
  }

  def subscriberRetrieve(id: String)(implicit ec: ExecutionContext): Future[Response[Subscriber]] =
    client.get(s"/data/subscribers/$id").map { httpResponse =>
      Response[Subscriber](httpResponse)
    }

  def subscriberCreate(subscriber: Subscriber)(implicit ec: ExecutionContext): Future[Response[Subscriber]] =
    for {
      entity <- Marshal(subscriber).to[RequestEntity]
      resp <- client.post(s"/data/subscribers", Nil, entity.withContentType(ContentTypes.`application/json`)).map { httpResponse =>
        val response = Response[Subscriber](httpResponse)
        logResponse(response)
        response
      }
    } yield resp

  def subscriberUpdate(user: Subscriber)(implicit ec: ExecutionContext): Future[Response[Subscriber]] =
    for {
      entity <- Marshal(user).to[RequestEntity]
      resp <- client.put(s"/data/subscribers/${user.id}", Nil, entity.withContentType(ContentTypes.`application/json`)).map { httpResponse =>
        val response = Response[Subscriber](httpResponse)
        logResponse(response)
        response
      }
    } yield resp

  def subscriberDelete(id: String)(implicit ec: ExecutionContext): Future[Response[NoEntity]] =
    client.delete(s"/data/subscribers/$id").map { httpResponse =>
      Response[NoEntity](httpResponse)
    }

  def subscriberList()(implicit ec: ExecutionContext): Future[Response[Subscribers]] =
    client.get(s"/data/subscribers").map { httpResponse =>
      val response = Response[Subscribers](httpResponse)
      logResponse(response)
      response
    }

}

class NewsletterServiceRestServiceClientImpl()(implicit val testConfig: TestConfig, val system: ActorSystem)
  extends NewsletterServiceRestServiceClient