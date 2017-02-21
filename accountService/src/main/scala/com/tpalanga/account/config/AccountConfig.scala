package com.tpalanga.account.config

import com.tpalanga.testlib.test.config.RestServiceConfig
import com.typesafe.config.Config

object AccountConfig {
  def apply(config: Config): AccountConfig =
    new AccountConfig(
      newsletterServiceConfig = RestServiceConfig(config.getConfig("service.newsletter"))
    )
}

case class AccountConfig(
                        newsletterServiceConfig: RestServiceConfig
                        )
