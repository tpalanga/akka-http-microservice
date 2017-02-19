package com.tpalanga.account.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class WebRoute extends BaseRoute {
  val route: Route =
    new PingRoute().route ~
      new ReadmeRoute().route
}
