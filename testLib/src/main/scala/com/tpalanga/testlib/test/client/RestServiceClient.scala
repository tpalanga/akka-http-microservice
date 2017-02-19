package com.tpalanga.testlib.test.client

import akka.actor.ActorSystem
import com.tpalanga.testlib.test.config.RestServiceConfig

trait RestServiceClient {

  val restServiceConfig: RestServiceConfig
  implicit val system: ActorSystem

  lazy val client = new RestClient(restServiceConfig)
}
