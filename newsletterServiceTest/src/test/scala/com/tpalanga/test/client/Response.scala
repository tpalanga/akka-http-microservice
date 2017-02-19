package com.tpalanga.test.client

import akka.http.scaladsl.model.{HttpResponse, StatusCode}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContext, Future}

case class Response[T](httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: Materializer, format: RootJsonFormat[T]) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  def isSuccess: Boolean = httpResponse.status.isSuccess
  def status: StatusCode = httpResponse.status
  def entity: Future[T] = Unmarshal(httpResponse.entity).to[T]
}
