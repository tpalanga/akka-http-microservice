package com.tpalanga.dataservice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.dataservice.route.WebRoute

object Bootstrap extends App {

  implicit val system = ActorSystem("dataservice-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  Http().bindAndHandle(new WebRoute().route, "localhost", 8081).map { httpServerBinding =>
    println(s"Dataservice online at http://localhost:8081/")
  }
}

