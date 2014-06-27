package io.floyd.web

import io.floyd.actors._

import spray.http.StatusCodes.Forbidden
import spray.http.{HttpResponse, _}
import spray.routing._
import spray.routing.authentication.UserPass
import spray.http.MediaTypes._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class FloydServiceActor extends HttpServiceActor with ActorLogging {

  import context.dispatcher // ExecutionContext for the futures and scheduler
  val allEventsActor = context.actorOf(Props[EventsActor], "all-events-actor")
  val userEventsActor = context.actorOf(Props[UserEventsActor], "user-events-actor")
  val tokenAuthActor = context.actorOf(Props[TokenAuthActor], "token-auth-actor")

  val authenticator = TokenAuthenticator[String](
    headerName = "X-Authorization",
    queryStringParameterName = "x_authorization"
  ) { key =>
    implicit val timeout = Timeout(5 seconds)
    (tokenAuthActor ? Token(key)).mapTo[Option[String]]
  }

  def auth: Directive1[String] = authenticate(authenticator)

  def receive = runRoute {
    path("ping") {
      complete {
        "PONG"
      }
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
    path("stream") {
      auth { user => ctx =>
        userEventsActor ! StartStreamForUser(user, ctx.responder)
      }
    } ~
    (path("updateUser") & post) {
      auth { user =>
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
    path("user" / "login") {
      formFields('user, 'pass).as(UserPass) { user =>
        implicit val timeout = Timeout(5 seconds)
        val futureResult = (tokenAuthActor ? user).mapTo[String]
        onComplete(futureResult) {
          case Success(authToken) => complete(authToken)
          case Failure(ex) => complete(Forbidden, s"Invalid User")
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


