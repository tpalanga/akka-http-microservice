package com.tpalanga.dataservice.route

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.tpalanga.dataservice.model.{User, UserDataStore}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
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
  import UserDataStore.DataFormats._
  import WebRouteSpec._

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(100, Millis)))

  // TODO (TP): use a tree like spec (FeatureSpec?)
  "WebRoute" should "respond to a GET request with an existing user" in new Test {
    Get(s"/data/users/$userId") ~> route ~> check {
      userService.expectMsg(UserDataStore.GetOne(userId))
      userService.reply(UserDataStore.OneUser(testUser))
      eventually {
        status shouldEqual StatusCodes.OK
      }
      responseAs[User] should be(testUser)
    }
  }

  it should "respond to a GET request with status 404 if the user does not exist" in new Test {
    Get(s"/data/users/$userId") ~> route ~> check {
      userService.expectMsg(UserDataStore.GetOne(userId))
      userService.reply(UserDataStore.NotFound(userId))
      eventually {
        status shouldEqual StatusCodes.NotFound
      }
    }
  }

  it should "respond to a GET request with status 500 if retrieving the user fails (times out)" in new Test {
    // this timeout should be longer than the ask timeout
    implicit val patienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(100, Millis)))
    Get(s"/data/users/$userId") ~> route ~> check {
      userService.expectMsg(UserDataStore.GetOne(userId))
      eventually {
        status shouldEqual StatusCodes.InternalServerError
      }
      responseAs[String] should startWith(s"Getting user with ID $userId from userService failed: Ask timed out")
    }
  }

}
