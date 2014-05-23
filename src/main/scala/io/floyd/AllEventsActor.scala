package io.floyd

import akka.actor.{ActorRef, ActorLogging, Actor}

class AllEventsActor extends Actor with ActorLogging {

  val nextValue = Iterator.from(1)

  def createStreamer(client:ActorRef) = {
    val newActor = context.actorOf(StreamerActor.props(client), createNameOfStreamer())
    log.info("new actor " + newActor.toString())
    newActor ! StartStream()
  }

  def createNameOfStreamer() = {
    "stream" + nextValue.next()
  }

  override def receive: Receive = {
    case client: ActorRef =>
      createStreamer(client)
    case Update(data) =>
      context.actorSelection("stream*") forward Update(data)
  }
}
