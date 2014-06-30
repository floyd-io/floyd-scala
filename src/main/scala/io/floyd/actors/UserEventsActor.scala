package io.floyd.actors

import akka.actor.{Actor, ActorRef, Props}

case class StartStreamForUser(user: String, client: ActorRef)
case class UpdateForUser(user:String, data: String)

class UserEventsActor extends Actor {
  def receive = {
    case StartStreamForUser(user, client) =>
      context.child(user) match {
        case Some(actor) => actor ! client
        case None =>
          val eventsActor = context.actorOf(Props[EventsActor], user)
          eventsActor ! client
      }
    case UpdateForUser(user, data) =>
      context.child(user) foreach { actor =>
        actor ! Update(data)
      }
  }

}
