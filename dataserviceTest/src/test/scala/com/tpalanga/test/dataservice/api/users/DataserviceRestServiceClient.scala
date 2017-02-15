package com.tpalanga.test.dataservice.api.users

import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import com.tpalanga.test.client.{Response, RestServiceClient}
import com.tpalanga.test.config.{RestServiceConfig, TestConfig}
import com.tpalanga.test.dataservice.api.users.model.User

import scala.concurrent.{ExecutionContext, Future}

trait DataserviceRestServiceClient extends RestServiceClient {
  import com.tpalanga.test.dataservice.api.users.model.UserJsonProtocol._

  val testConfig: TestConfig

  override val restServiceConfig: RestServiceConfig = testConfig.restServiceConfig
  private implicit val materializer: Materializer = ActorMaterializer(ActorMaterializerSettings(system))

  def userRetrieve(id: String)(implicit ec: ExecutionContext): Future[Response[User]] =
    client.get(s"/data/users/$id").map { response =>
      Response[User](response)
    }

}