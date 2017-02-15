package com.tpalanga.test.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import com.tpalanga.test.config.RestServiceConfig

import scala.collection.immutable.Seq
import scala.concurrent.Future

class RestClient(config: RestServiceConfig)(implicit system: ActorSystem) {

  private implicit val materializer: Materializer = ActorMaterializer(ActorMaterializerSettings(system))
  private val http = Http(system)

  protected def uriFor(path: String): Uri = {
    val portext =
      if ((config.port == 80 && config.protocol == "http")
        || (config.port == 443 && config.protocol == "https")) ""
      else s":${config.port}"
    Uri(s"${config.protocol}://${config.host}$portext$path")
  }

  protected def sendRequest(httpRequest: HttpRequest): Future[HttpResponse] =
    http.singleRequest(httpRequest)

  def get(path: String, headers: Seq[HttpHeader] = Nil): Future[HttpResponse] =
    sendRequest(HttpRequest(GET, uriFor(path), headers))

  def post(path: String, headers: Seq[HttpHeader] = Nil, entity: RequestEntity = HttpEntity.Empty): Future[HttpResponse] =
    sendRequest(HttpRequest(GET, uriFor(path), headers))

}
