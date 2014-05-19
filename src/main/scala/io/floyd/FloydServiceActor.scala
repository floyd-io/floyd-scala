package io.floyd

import akka.actor.{Actor,Props,ActorLogging, ActorRef}
import spray.routing._
import spray.http._
import MediaTypes._
import spray.can.Http
import HttpMethods._
import MediaTypes._
import scala.concurrent.duration._
import scala.collection.mutable


case class StartStream(client: ActorRef)

// simple case class whose instances we use as send confirmation message for streaming chunks
case class Ok(remaining: Int, client: ActorRef)

class StreamerActor extends Actor with ActorLogging {
  import context.dispatcher // ExecutionContext for the futures and scheduler

  // we use the successful sending of a chunk as trigger for scheduling the next chunk

  def receive = {
    case StartStream(client) =>
      client ! ChunkedResponseStart(HttpResponse(entity = "")).withAck(Ok(5, client))

    case Ok(0, client) =>
      log.info("Finalizing response stream ...")
      client ! MessageChunk("\nStopped...")
      client ! ChunkedMessageEnd

    case Ok(remaining, client) =>
      log.info("Sending response chunk ...")
      log.info(this.toString())
      log.info(self.toString())
      context.system.scheduler.scheduleOnce(1000 millis span) {
        val json = s"""{date: "${DateTime.now.toIsoDateTimeString.toString()}", count:"$remaining" }\n"""
        client ! MessageChunk(json).withAck(Ok(remaining - 1, client))
      }
  }

}
class FloydServiceActor extends HttpServiceActor {
  
  import context.dispatcher // ExecutionContext for the futures and scheduler

  val streamActor = context.actorOf(Props[StreamerActor], name="streamer-full")

  def receive = runRoute {
    path("ping") {
      complete {
        "PONG"
      }
    } ~
    path("stream") { ctx =>
      streamActor ! StartStream(ctx.responder)
    } ~
    path("stop") {
      complete {
        in(1.second) { context.system.shutdown() }
        "Shutting down in 1 second..."
      }
    } ~
    path("crash") {
      complete {
        sys.error("BOOM!")
      }
    } ~
    path("part2.html") { ctx =>
      streamActor ! StartStream(ctx.responder)
    } ~
    path("") {
      complete {
        index
      }
    }
  }

  lazy val index = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <body>
          <h1>Say hello to <i>floyd-scala</i>!</h1>
          <p>Defined resources:</p>
          <ul>
            <li><a href="/ping">/ping</a></li>
            <li><a href="/stream">/stream</a></li>
            <li><a href="/crash">/crash</a></li>
            <li><a href="/stop">/stop</a></li>
          </ul>
        </body>
      </html>.toString()
    )
  )

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    context.system.scheduler.scheduleOnce(duration)(body)

}


