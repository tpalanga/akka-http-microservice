package com.tpalanga.test.account.api.users

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, RequestEntity}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import com.tpalanga.test.account.api.users.model.{NewUser, User, Users}
import com.tpalanga.test.config.TestConfig
import com.tpalanga.testlib.test.client.{NoEntity, Response, RestServiceClient}
import com.tpalanga.testlib.test.config.RestServiceConfig
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class AccountServiceRestClient(val restServiceConfig: RestServiceConfig)
                              (implicit val testConfig: TestConfig, val system: ActorSystem)
extends RestServiceClient with LazyLogging {
  import NoEntity.DataFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.tpalanga.test.account.api.users.model.UserJsonProtocol._

  logger.debug(s"AccountServiceRestServiceClient: $restServiceConfig")
  private implicit val materializer: Materializer = ActorMaterializer(ActorMaterializerSettings(system))

  def userRetrieve(id: String)(implicit ec: ExecutionContext): Future[Response[User]] =
    client.get(s"/data/users/$id").map { httpResponse =>
      Response[User](httpResponse)
    }

  def userCreate(user: NewUser)(implicit ec: ExecutionContext): Future[Response[User]] =
    for {
      entity <- Marshal(user).to[RequestEntity]
      httpResponse <- client.post(s"/data/users", Nil, entity.withContentType(ContentTypes.`application/json`))
    } yield Response[User](httpResponse)

  def userUpdate(user: User)(implicit ec: ExecutionContext): Future[Response[User]] =
    for {
      entity <- Marshal(user).to[RequestEntity]
      httpResponse <- client.put(s"/data/users/${user.id}", Nil, entity.withContentType(ContentTypes.`application/json`))
    } yield Response[User](httpResponse)

  def userDelete(id: String)(implicit ec: ExecutionContext): Future[Response[NoEntity]] =
    client.delete(s"/data/users/$id").map { httpResponse =>
      Response[NoEntity](httpResponse)
    }

  def userList()(implicit ec: ExecutionContext): Future[Response[Users]] =
    client.get(s"/data/users").map { httpResponse =>
      Response[Users](httpResponse)
    }
}
