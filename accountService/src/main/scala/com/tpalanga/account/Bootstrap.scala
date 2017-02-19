package com.tpalanga.account

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.account.route.WebRoute

object Bootstrap extends App {

  implicit val system = ActorSystem("account-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  Http().bindAndHandle(new WebRoute().route, "localhost", 8080).map { httpServerBinding =>
    println(s"Server online at http://localhost:8080/")
  }
}
