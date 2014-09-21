package io.floyd.web

import io.floyd.actors._
import io.floyd.events._
import io.floyd.db._

import spray.http.StatusCodes.{Forbidden, Conflict, MethodNotAllowed}
import spray.http._
import spray.routing._
import spray.http.MediaTypes._
import spray.routing.authentication._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}

class FloydServiceActor extends HttpServiceActor with ActorLogging {

  import context.dispatcher // ExecutionContext for the futures and scheduler

  val allEventsActor = context.actorOf(Props[EventsActor], "all-events-actor")
  val userEventsActor = context.actorOf(Props[UserEventsActor], "user-events-actor")
  val tokenAuthActor = context.actorOf(Props[TokenAuthActor], "token-auth-actor")
  val deviceRegisterActor = context.actorOf(Props[DeviceRegisterActor], "device-register-actor")
  val deviceAuthActor = context.actorOf(Props[DeviceAuthActor], "device-auth-actor")
  val deviceEventsActor = context.actorOf(Props[DeviceEventsActor], "devices-events-actor")
  val userRegisterActor = context.actorOf(Props[UserRegisterActor], "user-register-actor")

  val lookupBus = LookupBusImpl.instance
  implicit val timeout = Timeout(5 seconds)

  val authenticator = TokenAuthenticator[String](
    headerName = "X-Authorization",
    queryStringParameterName = "x_authorization"
  ) { key =>
    (tokenAuthActor ? key).mapTo[Option[String]]
  }

  def authenticatorDevice(context: RequestContext): Future[Authentication[String]] = {
    val deviceId = context.request.uri.query.get("id")
    val serial = context.request.uri.query.get("serial")
    (deviceId, serial) match {
      case (Some(deviceIdValue), Some(serialValue)) =>
        (deviceAuthActor ? DeviceId(deviceIdValue,serialValue)).mapTo[Authentication[String]]
      case _ =>
        Future {
          Left(MissingQueryParamRejection("id,serial"))
        }
    }
  }
  def authDevice: Directive1[String] = authenticate(authenticatorDevice _)

  def auth: Directive1[String] = authenticate(authenticator)

  val route =
    path("ping") {
      complete {
        "PONG"
      }
    } ~
    (path("update") & post) {
      entity(as[String]) { data =>
        allEventsActor ! Update(data)
        complete { "sent update to all-events-actor\n" }
      }
    } ~
    path("part2.html") { ctx =>
      allEventsActor ! ctx.responder
    } ~
    pathPrefix("user") {
      auth { user =>
        (pathPrefix("me") | pathPrefix(user)) {
          path ("devices") { ctx =>
            userEventsActor ! StartStreamForUser(user, ctx.responder)
          }
        }
      } ~
      path("session") {
        formFields('user, 'pass).as(UserPass) { user =>
          val futureResult = (tokenAuthActor ? user).mapTo[Tuple2[String,String]]
          onComplete(futureResult) {
            case Success((authToken,id)) =>
              val map = Map("token"->authToken,"id"->id)
              complete(compact(render(map)))
            case Failure(ex) => complete(Forbidden, s"Invalid User")
          }
        }
      } ~
      post {
        formFields('user, 'pass).as(UserPass) { user =>
          onComplete(userRegisterActor ? user){
            case Success(x) =>
              complete { "user registrated\n" }
            case Failure(ex) =>
              complete(MethodNotAllowed, ex.getLocalizedMessage())
          }

        }
      }
    } ~
    (path("updateUser") & post) {
      auth { user =>
        entity(as[String]) { data =>
          userEventsActor ! UpdateForUser(user, data)
          complete { "sent update to user-events-actor\n" }
        }
      }
    } ~
    path("jsclient.html") {
      getFromResource("jsClient.html")
    } ~
    path("floyd-client.js") {
       getFromResource("floyd-client.js")
    } ~
    path("floyd.css") {
       getFromResource("floyd.css")
    } ~
    path("device" / "register") {
      auth { userId =>
        formFields('deviceId, 'serialNumber, 'description, 'typeOfDevice) {
          (deviceId, serialNumber, description, typeOfDevice) =>
          val futureRegistration = deviceRegisterActor ?
            RegisterDevice(deviceId, serialNumber, description, userId, typeOfDevice)
          onSuccess(futureRegistration) {
            case DeviceRegistered => complete("valid registration")
            case DeviceNotRegistered => complete(Conflict, s"already registered device")
          }
        }
      }
    } ~
    (path("device" / "update") & post) {
      authDevice { device =>
        formFields('property,'value) { (property, value) =>
          val propertyMap = Map("id"->device,property -> value)
          lookupBus.publish(MsgEnvelope("device=" + device, Update(propertyMap)))
          complete("update sent")
        }
      }
    } ~
    path("device" / "announce") {
      authDevice { device => { ctx =>
          deviceEventsActor ! CreateStreamDevice(device, ctx.responder)
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

  def receive = runRoute (route)

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


