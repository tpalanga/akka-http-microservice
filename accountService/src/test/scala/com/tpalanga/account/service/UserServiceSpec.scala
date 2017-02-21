package com.tpalanga.account.service

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.tpalanga.account.model.{NewUser, User}
import org.scalatest.{FlatSpecLike, Matchers, OptionValues}

object UserServiceSpec {

  abstract class Test(implicit val system: ActorSystem) {
    val newsletterService = TestProbe()
    val userService: ActorRef = system.actorOf(UserService.props(newsletterService.ref))
  }

  trait TestWithCreatedUsers extends Test with Matchers {
    val requester = TestProbe()
    val newUsers = Seq(
      NewUser("user 1", "user1@test.com"),
      NewUser("user 2", "user2@test.com"),
      NewUser("user 3", "user3@test.com")
    )
    val createdUsers = newUsers.map { user =>
      userService.tell(UserService.AddOne(user), requester.ref)
      val createdUser = requester.expectMsgType[UserService.OneUser]
      createdUser.user.name shouldBe user.name
      newsletterService.expectMsg(NewsletterService.Subscribe(createdUser.user))
      createdUser.user
    }
  }
}

class UserServiceSpec extends TestKit(ActorSystem("UserServiceSpec")) with FlatSpecLike with Matchers with ImplicitSender with OptionValues {
  import UserServiceSpec._

  "UserService" should "create user" in new Test {
    userService ! UserService.AddOne(NewUser("new user", "newuser@test.com"))

    val oneUser = expectMsgType[UserService.OneUser]
    oneUser.user.name shouldBe "new user"

    newsletterService.expectMsg(NewsletterService.Subscribe(oneUser.user))

    userService ! UserService.GetOne(oneUser.user.id)
    expectMsg(UserService.OneUser(oneUser.user))
  }

  it should "reply user already exists when attempting to add an user with a duplicate name" in new TestWithCreatedUsers {
    userService ! UserService.AddOne(NewUser("user 1", "user1@test.com"))
    expectMsg(UserService.AlreadyExists)
  }

  it should "retrieve an user that already exists" in new TestWithCreatedUsers {
    val testUser = createdUsers.headOption.value
    userService ! UserService.GetOne(testUser.id)
    expectMsg(UserService.OneUser(testUser))
  }

  it should "reply NotFound when attempting to retrieve a user that does not exist" in new TestWithCreatedUsers {
    val testUser = createdUsers.headOption.value
    userService ! UserService.GetOne("unknown")
    expectMsg(UserService.NotFound("unknown"))
  }

  it should "retrieve all users" in new TestWithCreatedUsers {
    userService ! UserService.GetAll
    val allUsers = expectMsgType[UserService.AllUsers]
    allUsers.users should contain theSameElementsAs createdUsers
  }

  it should "update user" in new TestWithCreatedUsers {
    val testUser = createdUsers.headOption.value
    val updatedUser = testUser.copy(name = "renamed")
    userService ! UserService.Update(updatedUser)
    expectMsg(UserService.OneUser(updatedUser))

    userService ! UserService.GetOne(updatedUser.id)
    expectMsg(UserService.OneUser(updatedUser))
  }

  it should "reply NotFound when attempting to update an inexistent user" in new TestWithCreatedUsers {
    val updatedUser = User("unknown", "renamed", "unknown@test.com")
    userService ! UserService.Update(updatedUser)
    expectMsg(UserService.NotFound("unknown"))
  }

  it should "delete user" in new TestWithCreatedUsers {
    val testUser = createdUsers.headOption.value
    userService ! UserService.Delete(testUser.id)
    expectMsg(UserService.Deleted(testUser.id))

    newsletterService.expectMsg(NewsletterService.Unsubscribe(testUser.id))

    userService ! UserService.GetOne(testUser.id)
    expectMsg(UserService.NotFound(testUser.id))
  }

  it should "reply NotFound when attempting to delete an inexistent user" in new TestWithCreatedUsers {
    userService ! UserService.Delete("unknown")
    expectMsg(UserService.NotFound("unknown"))
  }
}
