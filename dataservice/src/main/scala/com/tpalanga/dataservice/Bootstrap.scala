package com.tpalanga.dataservice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.dataservice.model.UserDataStore
import com.tpalanga.dataservice.route.WebRoute
import com.tpalanga.dataservice.util.UnhandledMessageWatcher

object Bootstrap extends App {

  implicit val system = ActorSystem("dataservice-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val unhandledMessageWatcher = system.actorOf(UnhandledMessageWatcher.props())

  val userService = system.actorOf(UserDataStore.props())
  Http().bindAndHandle(new WebRoute(userService).route, "localhost", 8081).map { httpServerBinding =>
    println(s"Dataservice online at http://localhost:8081/")
  }
}

