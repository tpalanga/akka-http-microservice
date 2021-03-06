package com.tpalanga.test.config

import com.tpalanga.testlib.test.config.RestServiceConfig
import com.typesafe.config.Config

object TestConfig {
  def apply(config: Config): TestConfig =
    new TestConfig(
      restServiceConfig = RestServiceConfig(config.getConfig("remote.service"))
    )
}

case class TestConfig(restServiceConfig: RestServiceConfig)
