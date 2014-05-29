package io.floyd

import akka.actor._
import spray.http.{HttpEntity, ChunkedResponseStart, HttpResponse, MessageChunk, SetRequestTimeout}
import scala.concurrent.duration._
import spray.can.Http
import spray.http.ContentTypes.`application/json`

case class StartStream()

case class Update(data: String)

object StreamerActor {
  def props(client: ActorRef): Props = Props(new StreamerActor(client))
}

class StreamerActor(client: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case StartStream() =>
      client ! SetRequestTimeout(10 minutes)
      client ! ChunkedResponseStart(
        HttpResponse(entity = HttpEntity(`application/json`, updateData("start")))
      )

    case x: Http.ConnectionClosed =>
      log.info("killing " + self.toString() )
      self ! PoisonPill

    case Update(data) =>
      client ! MessageChunk(updateData(data))
  }

  def updateData(data:String) = {
    s"""{ "data":"${data}" }\n"""
  }

}