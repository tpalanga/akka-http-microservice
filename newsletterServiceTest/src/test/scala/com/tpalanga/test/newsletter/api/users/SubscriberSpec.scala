package com.tpalanga.test.newsletter.api.users

import akka.http.scaladsl.model.StatusCodes
import com.tpalanga.test.newsletter.api.users.model.Subscriber
import com.tpalanga.test.spec.RestSpec
import org.scalatest.{AsyncFlatSpec, Matchers}

object SubscriberSpec {

  def newUUID(): String = java.util.UUID.randomUUID.toString

  def createNewSubscriber(): Subscriber = {
    val id = newUUID()
    Subscriber(id, s"user-$id", s"user-$id@test.com")
  }
}

class SubscriberSpec extends AsyncFlatSpec with Matchers with RestSpec with NewsletterServiceRestServiceClient {
  import SubscriberSpec._

  "NewsletterService" should "return 404 if a subscriber does not exist" in {
    subscriberRetrieve("unknown").map { reply =>
      reply.status shouldBe StatusCodes.NotFound
    }
  }

  it should "create subscriber" in {
    val newSubscriber = createNewSubscriber()

    for {
      replyCreate <- subscriberCreate(newSubscriber)
      subscriber <- replyCreate.entity
    } yield {
      replyCreate.status shouldBe StatusCodes.Created
      subscriber.name shouldBe newSubscriber.name
    }
  }

  it should "retrieve subscriber" in {
    val newSubscriber = createNewSubscriber()

    for {
      replyCreate <- subscriberCreate(newSubscriber)
      subscriber <- replyCreate.entity
      replyRetrieve <- subscriberRetrieve(subscriber.id)
      retrievedSubscriber <- replyRetrieve.entity
    } yield {
      replyCreate.status shouldBe StatusCodes.Created
      subscriber.name shouldBe newSubscriber.name
      retrievedSubscriber shouldBe subscriber
    }
  }

  it should "update subscriber" in {
    val newSubscriber = createNewSubscriber()

    for {
      replyCreate <- subscriberCreate(newSubscriber)
      subscriber <- replyCreate.entity
      _ = replyCreate.status shouldBe StatusCodes.Created
      _ = subscriber.name shouldBe newSubscriber.name

      updatingSubscriber = subscriber.copy(name = "new name")
      replyUpdate <- subscriberUpdate(updatingSubscriber)
      _ = replyUpdate.status shouldBe StatusCodes.OK
      subscriberUpdated <- replyUpdate.entity
      _ = subscriberUpdated shouldBe updatingSubscriber

      replyRetrieve <- subscriberRetrieve(subscriber.id)
      retrievedSubscriber <- replyRetrieve.entity
    } yield {
      replyRetrieve.status shouldBe StatusCodes.OK
      retrievedSubscriber shouldBe updatingSubscriber
    }
  }

  it should "delete subscriber" in {
    val newSubscriber = createNewSubscriber()

    for {
      replyCreate <- subscriberCreate(newSubscriber)
      subscriber <- replyCreate.entity
      _ = replyCreate.status shouldBe StatusCodes.Created

      replyDelete <- subscriberDelete(subscriber.id)
      _ = replyDelete.status shouldBe StatusCodes.OK

      replyRetrieve <- subscriberRetrieve(subscriber.id)
    } yield {
      replyRetrieve.status shouldBe StatusCodes.NotFound
    }
  }

  it should "list subscribers" in {
    val newSubscriber = createNewSubscriber()

    for {
      replyCreate <- subscriberCreate(newSubscriber)
      subscriber <- replyCreate.entity
      _ = replyCreate.status shouldBe StatusCodes.Created
      _ = subscriber.name shouldBe newSubscriber.name
      replyList <- subscriberList()
      subscriberList <- replyList.entity
    } yield {
      subscriber.name shouldBe newSubscriber.name
      subscriberList.subscribers should contain(subscriber)
    }
  }

}
