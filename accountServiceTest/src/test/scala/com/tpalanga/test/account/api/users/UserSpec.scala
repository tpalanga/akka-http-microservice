package com.tpalanga.test.account.api.users

import akka.http.scaladsl.model.StatusCodes
import com.tpalanga.test.account.api.users.model.NewUser
import com.tpalanga.test.newsletter.api.subscriber.NewsletterServiceRestServiceClientImpl
import com.tpalanga.test.spec.RestSpec
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

object UserSpec {

  def newUUID(): String = java.util.UUID.randomUUID.toString
  def newUsername() = s"user-${newUUID()}"

  def createNewUser(): NewUser = {
    val id = newUUID()
    NewUser(s"user-$id", s"user-$id@test.com")
  }
}

class UserSpec extends AsyncFlatSpec with Matchers with RestSpec {
  import UserSpec._

  val newsletter = new NewsletterServiceRestServiceClientImpl()
  val account = new AccountServiceRestServiceClientImpl()

  "Dataservice" should "return 404 if a user does not exist" in {
    account.userRetrieve("unknown").map { reply =>
      reply.status shouldBe StatusCodes.NotFound
    }
  }

  it should "create user and subscribe to newsletter" in {
    val newUser = createNewUser()

    for {
      replyCreate <- account.userCreate(newUser)
      user <- replyCreate.entity
      _ <- Future(Thread.sleep(200))
      replySubscriberRetrieve <- newsletter.subscriberRetrieve(user.id)
      _ = replySubscriberRetrieve.status shouldBe StatusCodes.OK
      subscriber <- replySubscriberRetrieve.entity
    } yield {
      user.name shouldBe newUser.name
      subscriber.name shouldBe user.name
      subscriber.email shouldBe user.email
    }
  }

  it should "retrieve user" in {
    val newUser = createNewUser()

    for {
      replyCreate <- account.userCreate(newUser)
      _ = replyCreate.status shouldBe StatusCodes.Created
      user <- replyCreate.entity
      replyRetrieve <- account.userRetrieve(user.id)
      _ = replyRetrieve.status shouldBe StatusCodes.OK
      retrievedUser <- replyRetrieve.entity
    } yield {
      user.name shouldBe newUser.name
      retrievedUser shouldBe user
    }
  }

  it should "update user" in {
    val newUser = createNewUser()

    for {
      replyCreate <- account.userCreate(newUser)
      user <- replyCreate.entity
      _ = replyCreate.status shouldBe StatusCodes.Created
      _ = user.name shouldBe newUser.name

      updatingUser = user.copy(name = "new name")
      replyUpdate <- account.userUpdate(updatingUser)
      _ = replyUpdate.status shouldBe StatusCodes.OK
      userUpdated <- replyUpdate.entity
      _ = userUpdated shouldBe updatingUser

      replyRetrieve <- account.userRetrieve(user.id)
      _ = replyRetrieve.status shouldBe StatusCodes.OK
      retrievedUser <- replyRetrieve.entity
    } yield {
      retrievedUser shouldBe updatingUser
    }
  }

  it should "delete user" in {
    val newUser = createNewUser()

    for {
      replyCreate <- account.userCreate(newUser)
      user <- replyCreate.entity
      _ = replyCreate.status shouldBe StatusCodes.Created

      replyDelete <- account.userDelete(user.id)
      _ = replyDelete.status shouldBe StatusCodes.OK
      _ <- Future(Thread.sleep(200))
      replySubscriberRetrieve <- newsletter.subscriberRetrieve(user.id)
      _ = replySubscriberRetrieve.status shouldBe StatusCodes.NotFound

      replyRetrieve <- account.userRetrieve(user.id)
    } yield {
      replyRetrieve.status shouldBe StatusCodes.NotFound
    }
  }

  it should "list users" in {
    val newUser = createNewUser()

    for {
      replyCreate <- account.userCreate(newUser)
      user <- replyCreate.entity
      _ = replyCreate.status shouldBe StatusCodes.Created
      _ = user.name shouldBe newUser.name
      replyList <- account.userList()
      userList <- replyList.entity
    } yield {
      user.name shouldBe newUser.name
      userList.users should contain(user)
    }
  }

}
