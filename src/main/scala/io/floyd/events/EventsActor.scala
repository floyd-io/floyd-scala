package io.floyd.events

import akka.actor.{Actor, ActorLogging, ActorRef}

trait NamedStreamChilds extends Actor {
  val nextValue = Iterator.from(1)

  def createNameOfStreamer() = "stream" + nextValue.next()

  def createChild(client: ActorRef) =
    context.actorOf(StreamerActor.props(client), createNameOfStreamer())
}

class EventsActor extends Actor with ActorLogging with NamedStreamChilds {

  override def receive: Receive = {
    case client: ActorRef => createChild(client) ! StartStream()
    case Update(data) =>
      context.actorSelection("stream*") forward Update(data)
  }
}
