package io.floyd

import akka.actor._
import spray.http.{ChunkedResponseStart, HttpResponse, MessageChunk,
  ChunkedMessageEnd, DateTime}
import spray.can.Http
import spray.http.HttpResponse
import spray.http.ChunkedResponseStart

case class StartStream()

// simple case class whose instances we use as send confirmation message for streaming chunks
case class Ok(remaining: Int, client: ActorRef)

case class Update(data: String)

object StreamerActor {
  def props(client: ActorRef): Props = Props(new StreamerActor(client))
}

class StreamerActor(client: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case StartStream() =>
      client ! ChunkedResponseStart(HttpResponse(entity = s"""{data:"start"}\n"""))

    case x: Http.ConnectionClosed =>
      log.info("killing " + self.toString() )
      self ! PoisonPill

    case Update(data) =>
      log.info("parent " + context.parent.toString())
      log.info("children list " + context.children.toString())
      val json = s"""{ data:"${data}" }\n"""
      client ! MessageChunk(json)
  }

}