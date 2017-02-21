package com.tpalanga.account.route

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Route, ValidationRejection}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.tpalanga.account.model.{NewUser, User}
import com.tpalanga.account.service.UserService
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}


object UserRouteSpec {
  val userId = "ABC-123"
  val testUser = User(userId, "my test user", "test@test.com")
  val newTestUser = NewUser("my test user", "test@test.com")

  abstract class Test(implicit system: ActorSystem) {
    protected val userService = TestProbe()
    protected val route: Route = new WebRoute(userService.ref).route
  }
}

class UserRouteSpec extends WordSpec with ScalatestRouteTest with SprayJsonSupport with Matchers with Eventually {
  import User.DataFormats._
  import UserRouteSpec._

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(100, Millis)))
  private val longPatienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(100, Millis)))

  "UserRoute" when {
    "receiving a user GET request" should {
      "respond with the user data if the user exists" in new Test {
        Get(s"/data/users/$userId") ~> route ~> check {
          userService.expectMsg(UserService.GetOne(userId))
          userService.reply(UserService.OneUser(testUser))
          eventually {
            status shouldEqual StatusCodes.OK
          }
          responseAs[User] should be(testUser)
        }
      }

      "respond with status 404 if the user does not exist" in new Test {
        Get(s"/data/users/$userId") ~> route ~> check {
          userService.expectMsg(UserService.GetOne(userId))
          userService.reply(UserService.NotFound(userId))
          eventually {
            status shouldEqual StatusCodes.NotFound
          }
        }
      }

      "respond with status 500 if retrieving the user fails (times out)" in new Test {
        // this timeout should be longer than the ask timeout
        private implicit val patienceConfig = longPatienceConfig
        Get(s"/data/users/$userId") ~> route ~> check {
          userService.expectMsg(UserService.GetOne(userId))
          eventually {
            status shouldEqual StatusCodes.InternalServerError
          }
          responseAs[String] should startWith(s"Getting user with ID $userId from userService failed: Ask timed out")
        }
      }
    }

    "receiving a user POST request (create new user)" should {
      "respond with the user data" in new Test {
        Post(s"/data/users", HttpEntity(ContentTypes.`application/json`, """{"name":"my test user", "email":"test@test.com"}""")) ~> route ~> check {
          userService.expectMsg(UserService.AddOne(newTestUser))
          userService.reply(UserService.OneUser(testUser))
          eventually {
            status shouldEqual StatusCodes.Created
          }
          responseAs[User] should be(testUser)
        }
      }

      "respond with validation rejection if the user already exists" in new Test {
        Post(s"/data/users", HttpEntity(ContentTypes.`application/json`, """{"name":"my test user", "email":"test@test.com"}""")) ~> route ~> check {
          userService.expectMsg(UserService.AddOne(newTestUser))
          userService.reply(UserService.AlreadyExists)
          eventually {
            rejection shouldEqual ValidationRejection("User already exists")
          }
        }
      }

      "respond with status 500 if adding the user fails (times out)" in new Test {
        // this timeout should be longer than the ask timeout
        private implicit val patienceConfig = longPatienceConfig
        Post(s"/data/users", HttpEntity(ContentTypes.`application/json`, """{"name":"my test user", "email":"test@test.com"}""")) ~> route ~> check {
          userService.expectMsg(UserService.AddOne(newTestUser))
          eventually {
            status shouldEqual StatusCodes.InternalServerError
          }
          responseAs[String] should startWith(s"Creation of user 'my test user' failed: Ask timed out")
        }
      }
    }

  }
}
