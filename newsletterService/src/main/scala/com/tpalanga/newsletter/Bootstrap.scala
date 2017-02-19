package com.tpalanga.newsletter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.newsletter.model.UserDataStore
import com.tpalanga.newsletter.route.WebRoute
import com.tpalanga.newsletter.util.UnhandledMessageWatcher

object Bootstrap extends App {

  implicit val system = ActorSystem("newsletter-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val unhandledMessageWatcher = system.actorOf(UnhandledMessageWatcher.props())

  val userService = system.actorOf(UserDataStore.props())
  Http().bindAndHandle(new WebRoute(userService).route, "localhost", 8081).map { httpServerBinding =>
    println(s"Dataservice online at http://localhost:8081/")
  }
}

