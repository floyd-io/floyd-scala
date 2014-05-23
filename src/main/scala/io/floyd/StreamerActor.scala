package io.floyd

import akka.actor._
import spray.http.{HttpEntity, ChunkedResponseStart, HttpResponse, MessageChunk, SetRequestTimeout}
import scala.concurrent.duration._
import spray.can.Http
import spray.http.ContentTypes.`application/json`

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
      client ! SetRequestTimeout(10 minutes)
      client ! ChunkedResponseStart(
        HttpResponse(entity = HttpEntity(`application/json`, s"""{data:"start"}\n""") )
      )

    case x: Http.ConnectionClosed =>
      log.info("killing " + self.toString() )
      self ! PoisonPill

    case Update(data) =>
      val json = s"""{ data:"${data}" }\n"""
      client ! MessageChunk(json)
  }

}