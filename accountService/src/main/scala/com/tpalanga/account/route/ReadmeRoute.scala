package com.tpalanga.account.route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route

class ReadmeRoute {
  val route: Route =
    path("readme") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>This is a readme for the lazy readers</h1>"))
      }
    }

}
