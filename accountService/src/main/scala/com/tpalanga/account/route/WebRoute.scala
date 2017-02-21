package com.tpalanga.account.route

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class WebRoute(userService: ActorRef) extends BaseRoute {
  val route: Route =
    new PingRoute().route ~
      new UserRoute(userService).route
}
