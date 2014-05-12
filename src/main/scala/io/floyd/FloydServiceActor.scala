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
        val json = s"""{
            date:"${DateTime.now.toIsoDateTimeString.toString()}",
            count:"$remaining"
          }
          """
        client ! MessageChunk(json).withAck(Ok(remaining - 1, client))
      }
  }

  // simple case class whose instances we use as send confirmation message for streaming chunks
  case class Ok(remaining: Int, client: ActorRef)
}
class FloydServiceActor extends Actor {
  
  import context.dispatcher // ExecutionContext for the futures and scheduler

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  val streamActor = context.actorOf(Props[StreamerActor], name="streamer-full")

  def receive = {
    // when a new  connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      println(Thread.currentThread)
      sender ! index

    case HttpRequest(GET, Uri.Path("/stream"), _, _, _) =>
      streamActor ! StartStream(sender)

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      println(Thread.currentThread)
      sender ! HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/stop"), _, _, _) =>
      sender ! HttpResponse(entity = "Shutting down in 1 second ...")
      sender ! Http.Close
      context.system.scheduler.scheduleOnce(1.second) { context.system.shutdown() }

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      println(Thread.currentThread)
      sender ! HttpResponse(entity = "About to throw an exception in the request handling actor, " +
        "which triggers an actor restart")
      sys.error("BOOM!")
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

  


}



  case class StartStream(client: ActorRef)

