package io.floyd

import akka.actor._
import spray.routing._
import spray.http._
import MediaTypes._
import scala.concurrent.duration._
import spray.http.HttpResponse

class FloydServiceActor extends HttpServiceActor with ActorLogging {

  import context.dispatcher // ExecutionContext for the futures and scheduler

  var nextStreamNumber: Integer = 0

  def createStreamer(client:ActorRef) = {
    log.info("about to create the actor for the stream")
    val newActor = context.actorOf(StreamerActor.props(client), createNameOfStreamer())
    log.info("new actor " + newActor.toString())
    newActor ! StartStream()
  }

  def createNameOfStreamer() = {
    val streamName = "stream" + nextStreamNumber
    nextStreamNumber = nextStreamNumber + 1
    streamName
  }

  def receive = runRoute {
    path("ping") {
      complete {
        "PONG"
      }
    } ~
    path("stream") { ctx =>
      createStreamer(ctx.responder)
    } ~
    (path("update") & post){
      entity(as[String]) { data =>
        complete {
          log.info("children list " + context.children.toString())
          context.actorSelection("stream*") ! Update(data)
          "sent update to all streams"
        }
      }
    } ~
    path("part2.html") { ctx =>
      createStreamer(ctx.responder)
    } ~
    path("jsclient.html") {
        getFromResource("jsClient.html")
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
            <li><a href="/update">post here /update</a></li>
          </ul>
        </body>
      </html>.toString()
    )
  )


  def in[U](duration: FiniteDuration)(body: => U): Unit =
    context.system.scheduler.scheduleOnce(duration)(body)

}


