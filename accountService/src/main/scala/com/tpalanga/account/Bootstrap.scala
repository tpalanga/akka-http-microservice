package com.tpalanga.account

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.account.route.WebRoute
import com.tpalanga.account.service.UserService

object Bootstrap extends App {

  implicit val system = ActorSystem("account-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val userService = system.actorOf(UserService.props())

  Http().bindAndHandle(new WebRoute(userService).route, "localhost", 8080).map { httpServerBinding =>
    println(s"Server online at http://localhost:8080/")
  }
}
