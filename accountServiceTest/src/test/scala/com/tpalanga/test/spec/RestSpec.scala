package com.tpalanga.test.spec

import akka.actor.ActorSystem
import com.tpalanga.test.config.TestConfig
import com.typesafe.config.ConfigFactory

trait RestSpec {
  private val configFile = sys.props.get("CONFIG").getOrElse("dev.conf")
  private val allConfig = ConfigFactory.load(s"conf/$configFile")
  val testConfig = TestConfig(allConfig.getConfig("test"))

  implicit val system: ActorSystem = ActorSystem()
}
