package com.tpalanga.microservice.route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route

class PingRoute {
  val route: Route =
    path("ping") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>pong</h1>"))
      }
    }

}
