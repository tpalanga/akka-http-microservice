package com.tpalanga.account.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.Eventually
import org.scalatest.{FlatSpec, Matchers}


object PingRouteSpec {
  trait Test {
    val pingRoute = new PingRoute
  }
}

class PingRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with Eventually {
  import PingRouteSpec._

  "PingRoute" should "respond to GET" in new Test {
    Get("/ping") ~> pingRoute.route ~> check {
      eventually {
        status shouldBe StatusCodes.OK
      }
      responseAs[String] shouldBe "<h1>pong</h1>"
    }
  }

}
