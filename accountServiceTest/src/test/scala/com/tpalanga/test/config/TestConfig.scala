package com.tpalanga.test.config

import com.tpalanga.testlib.test.config.RestServiceConfig
import com.typesafe.config.Config

object TestConfig {
  def apply(config: Config): TestConfig =
    new TestConfig(
      accountServiceConfig = RestServiceConfig(config.getConfig("remote.service.account")),
      newsletterServiceConfig = RestServiceConfig(config.getConfig("remote.service.newsletter"))
    )
}

case class TestConfig(
                       accountServiceConfig: RestServiceConfig,
                       newsletterServiceConfig: RestServiceConfig
                     )

