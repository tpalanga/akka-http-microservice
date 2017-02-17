package com.tpalanga.dataservice.route

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.tpalanga.dataservice.model.{User, UserDataStore}
import org.scalatest.concurrent.Eventually
import org.scalatest.{FlatSpec, Matchers}


object WebRouteSpec {
  val userId = "ABC-123"
  val testUser = User(userId, "my test user")

  abstract class Test(implicit system: ActorSystem) {
    protected val userService = TestProbe()
    protected val route: Route = new WebRoute(userService.ref).route
  }
}

class WebRouteSpec extends FlatSpec with ScalatestRouteTest with SprayJsonSupport with Matchers with Eventually {
  import WebRouteSpec._

  // TODO (TP): use a tree like spec (FeatureSpec?)
  "WebRoute" should "respond to a GET request with an existing user" in new Test {
    Get(s"data/users/$userId") ~> route ~> check {
      userService.expectMsg(UserDataStore.GetOne(userId))
      userService.reply(UserDataStore.OneUser(testUser))
      eventually { status shouldEqual StatusCodes.OK }
      responseAs[User] should be(testUser)
    }
  }

  it should "respond to a GET request with NotFound if the user does not exist" in new Test {
    userService.expectMsg(UserDataStore.GetOne(userId))
    userService.reply(UserDataStore.NotFound)
    eventually { status shouldEqual StatusCodes.NotFound }
  }
}
