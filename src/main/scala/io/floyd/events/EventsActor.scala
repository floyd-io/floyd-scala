package io.floyd.events

import akka.actor.{Actor, ActorLogging, ActorRef}

trait NamedChilds extends Actor {
  val nextValue = Iterator.from(1)

  def createNameOfStreamer() = "stream" + nextValue.next()

  def createChild(client: ActorRef) =
    context.actorOf(StreamerActor.props(client), createNameOfStreamer())
}

class EventsActor extends Actor with ActorLogging with  NamedChilds {

  def createStreamer(client:ActorRef) = {
    createChild(client) ! StartStream()
  }

  override def receive: Receive = {
    case client: ActorRef =>
      createStreamer(client)
    case Update(data) =>
      context.actorSelection("stream*") forward Update(data)
  }
}
