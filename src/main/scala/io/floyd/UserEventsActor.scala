package io.floyd

import akka.actor.{Props, ActorRef, Actor}

case class StartStreamForUser(user: String, client: ActorRef)
case class UpdateForUser(user:String, data: String)

class UserEventsActor extends Actor {
  def receive = {
    case StartStreamForUser(user, client) =>
      context.child(user) match {
        case Some(actor) =>
          actor ! client
        case None =>
          val eventsActor = context.actorOf(Props[EventsActor], user)
          eventsActor ! client
      }
    case UpdateForUser(user, data) =>
      context.actorSelection(user) ! Update(data)
  }

}
