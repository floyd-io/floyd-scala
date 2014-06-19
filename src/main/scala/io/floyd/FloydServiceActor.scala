package io.floyd

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import spray.routing._
import spray.http._
import MediaTypes._
import spray.http.HttpResponse
import spray.routing.authentication.{UserPass, BasicAuth}

import scala.concurrent.duration._
import scala.concurrent.Future

class FloydServiceActor extends HttpServiceActor with ActorLogging {

  import context.dispatcher // ExecutionContext for the futures and scheduler
  val allEventsActor = context.actorOf(Props[EventsActor], "all-events-actor")
  val authenticatorActor = context.actorOf(Props[AuthenticatorActor], "authenticator-actor")
  val userEventsActor = context.actorOf(Props[UserEventsActor], "user-events-actor")

  def userPassAuthenticator(userPass: Option[UserPass]): Future[Option[String]] = {
    implicit val timeout = Timeout(5 seconds)
    authenticatorActor ? userPass map { _.asInstanceOf[Option[String]] }
  }

  def receive = runRoute {
    path("ping") {
      complete {
        "PONG"
      }
    } ~
    path("stream") { ctx =>
      allEventsActor ! ctx.responder
    } ~
    (path("update") & post){
      entity(as[String]) { data =>
        complete {
          allEventsActor ! Update(data)
          "sent update to all-events-actor\n"
        }
      }
    } ~
    path("part2.html") { ctx =>
      allEventsActor ! ctx.responder
    } ~
    path("streamUser") {
      authenticate(BasicAuth(userPassAuthenticator _, "user area")) { (user) =>
        { ctx =>
          userEventsActor ! StartStreamForUser(user, ctx.responder)
        }
      }
    } ~
    (path("updateUser") & post) {
      authenticate(BasicAuth(userPassAuthenticator _, "user area")) { user =>
        entity(as[String]) { data =>
          complete {
            userEventsActor ! UpdateForUser(user, data)
            "sent update to user-events-actor\n"
          }
        }
      }
    } ~
    path("jsclient.html") {
      getFromResource("jsClient.html")
    } ~
    path("validateUser") {
      authenticate(BasicAuth(userPassAuthenticator _, "user area")) { user =>
        complete {
          "validated user"
        }
      }
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
            <li><a href="/jsclient.html">/jsclient.html</a></li>
            <li><a href="/update">post here /update</a></li>
          </ul>
        </body>
      </html>.toString()
    )
  )


  def in[U](duration: FiniteDuration)(body: => U): Unit =
    context.system.scheduler.scheduleOnce(duration)(body)

}


