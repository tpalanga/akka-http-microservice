package com.tpalanga.newsletter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.newsletter.model.SubscriberService
import com.tpalanga.newsletter.route.SubscriberRoute
import com.tpalanga.newsletter.util.UnhandledMessageWatcher
import com.typesafe.scalalogging.LazyLogging

object Bootstrap extends App with LazyLogging {

  implicit val system = ActorSystem("newsletter-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val unhandledMessageWatcher = system.actorOf(UnhandledMessageWatcher.props())

  val subscriberService = system.actorOf(SubscriberService.props())
  Http().bindAndHandle(new SubscriberRoute(subscriberService).route, "0.0.0.0", 8081).map { httpServerBinding =>
    logger.info(s"Newsletter service online at http://localhost:8081/")
  }
}

