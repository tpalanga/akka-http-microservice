package com.tpalanga.account.route

import akka.http.scaladsl.server.Route

trait BaseRoute {
  val route: Route
}
