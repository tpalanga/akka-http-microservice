package com.tpalanga.dataservice.model

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.event.Logging.LogEvent
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{FlatSpecLike, Matchers, OptionValues}

object UserDataStoreSpec {

  abstract class Test(implicit val system: ActorSystem) {
    val userDataStore: ActorRef = system.actorOf(UserDataStore.props())
  }

  trait TestWithCreatedUsers extends Test with Matchers {
    val requester = TestProbe()
    val newUsers = Seq(
      NewUser("user 1"),
      NewUser("user 2"),
      NewUser("user 3")
    )
    val createdUsers = newUsers.map { user =>
      userDataStore.tell(UserDataStore.AddOne(user), requester.ref)
      val createdUser = requester.expectMsgType[UserDataStore.OneUser]
      createdUser.user.name shouldBe user.name
      createdUser.user
    }
  }
}

class UserDataStoreSpec extends TestKit(ActorSystem("UserDataStoreSpec")) with FlatSpecLike with Matchers with ImplicitSender with OptionValues {
  import UserDataStoreSpec._

  "UserDataStore" should "create user" in new Test {
    userDataStore ! UserDataStore.AddOne(NewUser("new user"))

    val oneUser = expectMsgType[UserDataStore.OneUser]
    oneUser.user.name shouldBe "new user"

    userDataStore ! UserDataStore.GetOne(oneUser.user.id)
    expectMsg(UserDataStore.OneUser(oneUser.user))
  }

  it should "reply user already exists when attempting to add an user with a duplicate name" in new TestWithCreatedUsers {
    userDataStore ! UserDataStore.AddOne(NewUser("user 1"))
    expectMsg(UserDataStore.AlreadyExists)
  }

  it should "retrieve an user that already exists" in new TestWithCreatedUsers {
    val testUser = createdUsers.headOption.value
    userDataStore ! UserDataStore.GetOne(testUser.id)
    expectMsg(UserDataStore.OneUser(testUser))
  }

  it should "reply NotFound when attempting to retrieve a user that does not exist" in new TestWithCreatedUsers {
    val testUser = createdUsers.headOption.value
    userDataStore ! UserDataStore.GetOne("unknown")
    expectMsg(UserDataStore.NotFound("unknown"))
  }

  it should "retrieve all users" in new TestWithCreatedUsers {
    userDataStore ! UserDataStore.GetAll
    val allUsers = expectMsgType[UserDataStore.AllUsers]
    allUsers.users should contain theSameElementsAs createdUsers
  }

  it should "update user" in new TestWithCreatedUsers {
    val testUser = createdUsers.headOption.value
    val updatedUser = testUser.copy(name = "renamed")
    userDataStore ! UserDataStore.Update(updatedUser)
    expectMsg(UserDataStore.OneUser(updatedUser))

    userDataStore ! UserDataStore.GetOne(updatedUser.id)
    expectMsg(UserDataStore.OneUser(updatedUser))
  }

  it should "reply NotFound when attempting to update an inexistent user" in new TestWithCreatedUsers {
    val updatedUser = User("unknown", "renamed")
    userDataStore ! UserDataStore.Update(updatedUser)
    expectMsg(UserDataStore.NotFound("unknown"))
  }

  it should "delete user" in new TestWithCreatedUsers {
    val testUser = createdUsers.headOption.value
    userDataStore ! UserDataStore.Delete(testUser.id)
    expectMsg(UserDataStore.Deleted(testUser.id))

    userDataStore ! UserDataStore.GetOne(testUser.id)
    expectMsg(UserDataStore.NotFound(testUser.id))
  }

  it should "reply NotFound when attempting to delete an inexistent user" in new TestWithCreatedUsers {
    userDataStore ! UserDataStore.Delete("unknown")
    expectMsg(UserDataStore.NotFound("unknown"))
  }

  it should "log a message when receiving a message that is not handled" in new Test {
    val logProbe = TestProbe()
    system.eventStream.subscribe(logProbe.ref, classOf[LogEvent])

    userDataStore ! 'Unexpected

    val logEvent = logProbe.expectMsgType[LogEvent]
    logEvent.level shouldBe Logging.WarningLevel
    logEvent.message shouldBe "Unhandled message 'Unexpected"
  }
}
