package com.tpalanga.test.dataservice.api.users

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, RequestEntity}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import com.tpalanga.test.client.{Response, RestServiceClient}
import com.tpalanga.test.config.{RestServiceConfig, TestConfig}
import com.tpalanga.test.dataservice.api.users.model.{NewUser, NoEntity, User}

import scala.concurrent.{Await, ExecutionContext, Future}

trait DataserviceRestServiceClient extends RestServiceClient {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.tpalanga.test.dataservice.api.users.model.UserJsonProtocol._

  val testConfig: TestConfig

  override val restServiceConfig: RestServiceConfig = testConfig.restServiceConfig
  private implicit val materializer: Materializer = ActorMaterializer(ActorMaterializerSettings(system))

  private def logResponse[T](response: Response[T]) = {
    // TODO (TP): use a logger
    println(response)
    import scala.concurrent.duration._
    println(Await.result(response.entity, 100.millis))
    response
  }

  def userRetrieve(id: String)(implicit ec: ExecutionContext): Future[Response[User]] =
    client.get(s"/data/users/$id").map { httpResponse =>
      Response[User](httpResponse)
    }

  def userCreate(user: NewUser)(implicit ec: ExecutionContext): Future[Response[User]] =
    for {
      entity <- Marshal(user).to[RequestEntity]
      resp <- client.post(s"/data/users", Nil, entity.withContentType(ContentTypes.`application/json`)).map { httpResponse =>
        val response = Response[User](httpResponse)
        logResponse(response)
        response
      }
    } yield resp

  def userUpdate(user: User)(implicit ec: ExecutionContext): Future[Response[User]] =
    for {
      entity <- Marshal(user).to[RequestEntity]
      resp <- client.put(s"/data/users/${user.id}", Nil, entity.withContentType(ContentTypes.`application/json`)).map { httpResponse =>
        val response = Response[User](httpResponse)
        logResponse(response)
        response
      }
    } yield resp

  def userDelete(id: String)(implicit ec: ExecutionContext): Future[Response[NoEntity]] =
    client.delete(s"/data/users/$id").map { httpResponse =>
      Response[NoEntity](httpResponse)
    }

  def userList()(implicit ec: ExecutionContext): Future[Response[Seq[User]]] =
    client.get(s"/data/users").map { httpResponse =>
      val response = Response[Seq[User]](httpResponse)
      logResponse(response)
      response
    }

}