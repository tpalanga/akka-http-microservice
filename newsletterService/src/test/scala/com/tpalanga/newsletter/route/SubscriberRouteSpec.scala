package com.tpalanga.newsletter.route

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Route, ValidationRejection}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.tpalanga.newsletter.model.{Subscriber, SubscriberService}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}


object SubscriberRouteSpec {
  val userId = "ABC-123"
  val testSubscriber = Subscriber(userId, "my test user", "test@test.com")

  abstract class Test(implicit system: ActorSystem) {
    protected val subscriberService = TestProbe()
    protected val route: Route = new SubscriberRoute(subscriberService.ref).route
  }
}

class SubscriberRouteSpec extends WordSpec with ScalatestRouteTest with SprayJsonSupport with Matchers with Eventually {
  import Subscriber.DataFormats._
  import SubscriberRouteSpec._

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(100, Millis)))
  private val longPatienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(100, Millis)))

  "SubscriberRoute" when {
    "receiving a subscriber GET request" should {
      "respond with the subscriber data if the subscriber exists" in new Test {
        Get(s"/data/subscribers/$userId") ~> route ~> check {
          subscriberService.expectMsg(SubscriberService.GetOne(userId))
          subscriberService.reply(SubscriberService.OneSubscriber(testSubscriber))
          eventually {
            status shouldEqual StatusCodes.OK
          }
          responseAs[Subscriber] should be(testSubscriber)
        }
      }

      "respond with status 404 if the subscriber does not exist" in new Test {
        Get(s"/data/subscribers/$userId") ~> route ~> check {
          subscriberService.expectMsg(SubscriberService.GetOne(userId))
          subscriberService.reply(SubscriberService.NotFound(userId))
          eventually {
            status shouldEqual StatusCodes.NotFound
          }
        }
      }

      "respond with status 500 if retrieving the subscriber fails (times out)" in new Test {
        // this timeout should be longer than the ask timeout
        private implicit val patienceConfig = longPatienceConfig
        Get(s"/data/subscribers/$userId") ~> route ~> check {
          subscriberService.expectMsg(SubscriberService.GetOne(userId))
          eventually {
            status shouldEqual StatusCodes.InternalServerError
          }
          responseAs[String] should startWith(s"Getting subscriber with ID $userId from subscriberService failed: Ask timed out")
        }
      }
    }

    "receiving a subscriber POST request (create new subscriber)" should {
      "respond with the subscriber data" in new Test {
        Post(s"/data/subscribers", HttpEntity(ContentTypes.`application/json`, """{"id":"ABC-123", "name":"my test user", "email":"test@test.com"}""")) ~> route ~> check {
          subscriberService.expectMsg(SubscriberService.AddOne(testSubscriber))
          subscriberService.reply(SubscriberService.OneSubscriber(testSubscriber))
          eventually {
            status shouldEqual StatusCodes.Created
          }
          responseAs[Subscriber] should be(testSubscriber)
        }
      }

      "respond with validation rejection if the subscriber already exists" in new Test {
        Post(s"/data/subscribers", HttpEntity(ContentTypes.`application/json`, """{"id":"ABC-123", "name":"my test user", "email":"test@test.com"}""")) ~> route ~> check {
          subscriberService.expectMsg(SubscriberService.AddOne(testSubscriber))
          subscriberService.reply(SubscriberService.AlreadyExists)
          eventually {
            rejection shouldEqual ValidationRejection("Subscriber already exists")
          }
        }
      }

      "respond with status 500 if adding the subscriber fails (times out)" in new Test {
        // this timeout should be longer than the ask timeout
        private implicit val patienceConfig = longPatienceConfig
        Post(s"/data/subscribers", HttpEntity(ContentTypes.`application/json`, """{"id":"ABC-123", "name":"my test user", "email":"test@test.com"}""")) ~> route ~> check {
          subscriberService.expectMsg(SubscriberService.AddOne(testSubscriber))
          eventually {
            status shouldEqual StatusCodes.InternalServerError
          }
          responseAs[String] should startWith(s"Creation of subscriber with ID 'ABC-123' failed: Ask timed out")
        }
      }
    }

  }
}
