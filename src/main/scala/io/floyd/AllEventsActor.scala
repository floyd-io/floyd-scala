package io.floyd

import akka.actor.{ActorRef, ActorLogging, Actor}

class AllEventsActor extends Actor with ActorLogging {

  var nextStreamNumber: Integer = 0

  def createStreamer(client:ActorRef) = {
    val newActor = context.actorOf(StreamerActor.props(client), createNameOfStreamer())
    log.info("new actor " + newActor.toString())
    newActor ! StartStream()
  }

  def createNameOfStreamer() = {
    val streamName = "stream" + nextStreamNumber
    nextStreamNumber = nextStreamNumber + 1
    streamName
  }

  override def receive: Receive = {
    case client: ActorRef =>
      createStreamer(client)
    case Update(data) =>
      context.actorSelection("stream*") forward Update(data)
  }
}
