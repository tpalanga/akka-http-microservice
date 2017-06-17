package com.tpalanga.test.spec

import akka.actor.ActorSystem
import com.tpalanga.test.config.TestConfig
import com.typesafe.config.ConfigFactory

trait RestSpec {
  private val configFile = sys.props.getOrElse("CONFIG", sys.env.getOrElse("CONFIG", "dev.conf"))
  private val allConfig = ConfigFactory.load(s"conf/$configFile")
  implicit val testConfig = TestConfig(allConfig.getConfig("test"))

  implicit val system: ActorSystem = ActorSystem()
}
