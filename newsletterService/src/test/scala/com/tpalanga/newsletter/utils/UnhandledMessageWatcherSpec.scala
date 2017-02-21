package com.tpalanga.newsletter.utils

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging.LogEvent
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.tpalanga.newsletter.util.UnhandledMessageWatcher
import org.scalatest.{FlatSpecLike, Matchers}

object UnhandledMessageWatcherSpec {

  abstract class Test(implicit system: ActorSystem) {
    val watcher = system.actorOf(UnhandledMessageWatcher.props())
    val logProbe = TestProbe()
    system.eventStream.subscribe(logProbe.ref, classOf[LogEvent])

    val destination = system.actorOf(Props(new Actor {
      override def receive: Receive = {
        case 'Handled =>
      }
    }))

  }
}

class UnhandledMessageWatcherSpec extends TestKit(ActorSystem("UnhandledMessageWatcherSpec")) with FlatSpecLike with Matchers with ImplicitSender {
  import UnhandledMessageWatcherSpec._

  "UnhandledMessageWatcher" should "log unhandled messages" in new Test {
    destination ! 'Unhandled

    val event = logProbe.fishForMessage() {
      case akka.event.Logging.Error(_, _, _, msg) if msg.toString startsWith "UnhandledMessage:" =>
        true
      case _ =>
        false
    }
  }

  it should "log DeadLetters" in new Test {
    system.stop(destination)
    Thread.sleep(100)
    destination ! 'Handled

    val event = logProbe.fishForMessage() {
      case akka.event.Logging.Warning(_, _, msg) if msg.toString startsWith "DeadLetter:" =>
        true
      case _ =>
        false
    }
  }
}
