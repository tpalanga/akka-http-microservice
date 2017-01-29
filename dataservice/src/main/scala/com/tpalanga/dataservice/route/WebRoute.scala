package com.tpalanga.dataservice.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class WebRoute {
  val route: Route =
    pathPrefix("data") {
      path(Segment) { id =>
        pathEnd {
          get {
            // get id
            complete(StatusCodes.NotImplemented)
          }
        }
      } ~
      pathEnd {
        get {
          // list
          complete(StatusCodes.NotImplemented)
        } ~
          post {
            // create
            complete(StatusCodes.NotImplemented)
          }
      }
    }
}
