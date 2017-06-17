package com.tpalanga.account

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.account.config.AccountConfig
import com.tpalanga.account.route.WebRoute
import com.tpalanga.account.service.{NewsletterService, UserService}
import com.typesafe.config.ConfigFactory

object Bootstrap extends App {

  implicit val system = ActorSystem("account-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val configFile = sys.props.getOrElse("CONFIG", sys.env.getOrElse("CONFIG", "dev.conf"))
  println(s"Starting with config file: $configFile")
  private val allConfig = ConfigFactory.load(s"conf/$configFile")
  val accountConfig = AccountConfig(allConfig)
  val newsletterService = system.actorOf(NewsletterService.props(accountConfig.newsletterServiceConfig))
  val userService = system.actorOf(UserService.props(newsletterService))

  Http().bindAndHandle(new WebRoute(userService).route, "0.0.0.0", 8080).map { httpServerBinding =>
    println(s"Account service online at http://localhost:8080/")
  }
}
