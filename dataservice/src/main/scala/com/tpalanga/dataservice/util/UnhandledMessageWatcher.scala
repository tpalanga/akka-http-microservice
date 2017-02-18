package com.tpalanga.dataservice.util

import akka.actor.{Actor, ActorLogging, DeadLetter, Props, UnhandledMessage}

object UnhandledMessageWatcher {
  def props() = Props(new UnhandledMessageWatcher)
}

class UnhandledMessageWatcher extends Actor with ActorLogging {

  context.system.eventStream.subscribe(self, classOf[DeadLetter])
  context.system.eventStream.subscribe(self, classOf[UnhandledMessage])

  override def receive: Receive = {
    case UnhandledMessage(msg, msgSender, recipient) =>
      log.error(s"UnhandledMessage: $msgSender sent $msg to $recipient")

    case DeadLetter(msg, msgSender, recipient) =>
      log.warning(s"DeadLetter: $msgSender sent $msg to $recipient")
  }
}
