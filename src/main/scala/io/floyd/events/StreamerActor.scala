package io.floyd.events

import akka.actor._
import spray.can.Http
import spray.http.ContentTypes.`application/json`
import spray.http.{ChunkedResponseStart, HttpEntity, HttpResponse, MessageChunk, SetRequestTimeout}
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write


import scala.concurrent.duration._

case class StartStream()
case class Update(data: AnyRef)
case class RegisterListener(selector: String)

object StreamerActor {
  def props(client: ActorRef): Props = Props(new StreamerActor(client))
}

class StreamerActor(client: ActorRef) extends Actor with ActorLogging {
  val lookupBus = LookupBusImpl.instance

  def receive = {
    case StartStream() =>
      client ! SetRequestTimeout(10 minutes)
      client ! ChunkedResponseStart(
        HttpResponse(entity = HttpEntity(`application/json`, ""))
      )

    case x: Http.ConnectionClosed =>
      log.info("killing " + self.toString() )
      self ! PoisonPill

    case Update(data) =>
      client ! MessageChunk(updateData(data))

    case RegisterListener(selector) =>
      log.debug("new listener on "+selector)
      lookupBus.subscribe(self, selector)
  }

  def updateData(data:AnyRef) = {
    implicit val formats = Serialization.formats(NoTypeHints)
    write(data) + "\n"
  }

}
