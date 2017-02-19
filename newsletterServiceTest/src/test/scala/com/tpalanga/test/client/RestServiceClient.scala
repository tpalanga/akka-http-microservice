package com.tpalanga.test.client

import akka.actor.ActorSystem
import com.tpalanga.test.config.RestServiceConfig

trait RestServiceClient {

  val restServiceConfig: RestServiceConfig
  implicit val system: ActorSystem

  lazy val client = new RestClient(restServiceConfig)
}
