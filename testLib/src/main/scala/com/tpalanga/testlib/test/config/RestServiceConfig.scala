package com.tpalanga.testlib.test.config

import com.typesafe.config.Config

object RestServiceConfig {
  def apply(config: Config): RestServiceConfig =
    new RestServiceConfig(
      host = config.getString("host"),
      protocol = config.getString("protocol"),
      port = config.getInt("port")
    )
}

case class RestServiceConfig(host: String, protocol: String, port: Int)
