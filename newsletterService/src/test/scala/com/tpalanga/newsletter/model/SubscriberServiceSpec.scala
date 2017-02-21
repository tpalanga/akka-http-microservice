package com.tpalanga.newsletter.model

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.event.Logging.LogEvent
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{FlatSpecLike, Matchers, OptionValues}

object SubscriberServiceSpec {

  abstract class Test(implicit val system: ActorSystem) {
    val subscriberService: ActorRef = system.actorOf(SubscriberService.props())
  }

  trait TestWithCreatedSubscribers extends Test with Matchers {
    val requester = TestProbe()
    val newSubscribers = Seq(
      Subscriber("u01", "user 1", "user1@test.com"),
      Subscriber("u02", "user 2", "user2@test.com"),
      Subscriber("u03", "user 3", "user3@test.com")
    )
    val createdSubscribers = newSubscribers.map { subscriber =>
      subscriberService.tell(SubscriberService.AddOne(subscriber), requester.ref)
      val createdSubscriber = requester.expectMsgType[SubscriberService.OneSubscriber]
      createdSubscriber.subscriber.name shouldBe subscriber.name
      createdSubscriber.subscriber
    }
  }
}

class SubscriberServiceSpec extends TestKit(ActorSystem("SubscriberServiceSpec")) with FlatSpecLike with Matchers with ImplicitSender with OptionValues {
  import SubscriberServiceSpec._

  "SubscriberService" should "create subscriber" in new Test {
    subscriberService ! SubscriberService.AddOne(Subscriber("new123", "new user", "new_user123@test.com"))

    val oneSubscriber = expectMsgType[SubscriberService.OneSubscriber]
    oneSubscriber.subscriber.name shouldBe "new user"

    subscriberService ! SubscriberService.GetOne(oneSubscriber.subscriber.id)
    expectMsg(SubscriberService.OneSubscriber(oneSubscriber.subscriber))
  }

  it should "reply subscriber already exists when attempting to add an subscriber with a duplicate ID" in new TestWithCreatedSubscribers {
    subscriberService ! SubscriberService.AddOne(Subscriber("u01", "user XYZ", "userXYZ@test.com"))
    expectMsg(SubscriberService.AlreadyExists)
  }

  it should "retrieve a subscriber that already exists" in new TestWithCreatedSubscribers {
    val testSubscriber = createdSubscribers.headOption.value
    subscriberService ! SubscriberService.GetOne(testSubscriber.id)
    expectMsg(SubscriberService.OneSubscriber(testSubscriber))
  }

  it should "reply NotFound when attempting to retrieve a subscriber that does not exist" in new TestWithCreatedSubscribers {
    val testSubscriber = createdSubscribers.headOption.value
    subscriberService ! SubscriberService.GetOne("unknown")
    expectMsg(SubscriberService.NotFound("unknown"))
  }

  it should "retrieve all subscribers" in new TestWithCreatedSubscribers {
    subscriberService ! SubscriberService.GetAll
    val allSubscribers = expectMsgType[SubscriberService.AllSubscribers]
    allSubscribers.subscribers should contain theSameElementsAs createdSubscribers
  }

  it should "update subscriber" in new TestWithCreatedSubscribers {
    val testSubscriber = createdSubscribers.headOption.value
    val updatedSubscriber = testSubscriber.copy(name = "renamed")
    subscriberService ! SubscriberService.Update(updatedSubscriber)
    expectMsg(SubscriberService.OneSubscriber(updatedSubscriber))

    subscriberService ! SubscriberService.GetOne(updatedSubscriber.id)
    expectMsg(SubscriberService.OneSubscriber(updatedSubscriber))
  }

  it should "reply NotFound when attempting to update an inexistent subscriber" in new TestWithCreatedSubscribers {
    val updatedSubscriber = Subscriber("unknown", "renamed", "unknown@test.com")
    subscriberService ! SubscriberService.Update(updatedSubscriber)
    expectMsg(SubscriberService.NotFound("unknown"))
  }

  it should "delete subscriber" in new TestWithCreatedSubscribers {
    val testSubscriber = createdSubscribers.headOption.value
    subscriberService ! SubscriberService.Delete(testSubscriber.id)
    expectMsg(SubscriberService.Deleted(testSubscriber.id))

    subscriberService ! SubscriberService.GetOne(testSubscriber.id)
    expectMsg(SubscriberService.NotFound(testSubscriber.id))
  }

  it should "reply NotFound when attempting to delete an inexistent subscriber" in new TestWithCreatedSubscribers {
    subscriberService ! SubscriberService.Delete("unknown")
    expectMsg(SubscriberService.NotFound("unknown"))
  }

  it should "log a message when receiving a message that is not handled" in new Test {
    val logProbe = TestProbe()
    system.eventStream.subscribe(logProbe.ref, classOf[LogEvent])

    subscriberService ! 'Unexpected

    val logEvent = logProbe.expectMsgType[LogEvent]
    logEvent.level shouldBe Logging.WarningLevel
    logEvent.message shouldBe "Unhandled message 'Unexpected"
  }
}
