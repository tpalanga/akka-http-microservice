package com.tpalanga.microservice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object Bootstrap extends App {

  implicit val system = ActorSystem("akka-http-microservice-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route =
    path("ping") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>pong</h1>"))
      }
    }

  Http().bindAndHandle(route, "localhost", 8080).map { httpServerBinding =>
    println(s"Server online at http://localhost:8080/")
  }

}
