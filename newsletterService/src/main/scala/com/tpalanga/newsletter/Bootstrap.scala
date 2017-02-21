package com.tpalanga.newsletter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.newsletter.model.SubscriberService
import com.tpalanga.newsletter.route.SubscriberRoute
import com.tpalanga.newsletter.util.UnhandledMessageWatcher

object Bootstrap extends App {

  implicit val system = ActorSystem("newsletter-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val unhandledMessageWatcher = system.actorOf(UnhandledMessageWatcher.props())

  val subscriberService = system.actorOf(SubscriberService.props())
  Http().bindAndHandle(new SubscriberRoute(subscriberService).route, "localhost", 8081).map { httpServerBinding =>
    println(s"Dataservice online at http://localhost:8081/")
  }
}

