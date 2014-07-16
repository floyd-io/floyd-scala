package io.floyd.events

import io.floyd.db.{DevicesActor, Device}

import akka.actor.{Actor, ActorRef, Props, ActorLogging}
import akka.util.Timeout
import akka.pattern.{ask, pipe}

import scala.concurrent.duration._

case class StartStreamForUser(user: String, client: ActorRef)
case class UpdateForUser(user:String, data: AnyRef)

class UserEventsActor extends Actor with ActorLogging with NamedStreamChilds {

  val lookupBus = LookupBusImpl.instance

  val devicesActor = context.actorOf(Props[DevicesActor], "devices-actor")

  import context.dispatcher

  def receive = {
    case StartStreamForUser(user, client) =>
      val newStreamActor = createChild(client)
      newStreamActor ! StartStream()
      newStreamActor ! RegisterListener("user="+user)
      implicit val timeout = Timeout(5 seconds)
      val devices = (devicesActor ? user).mapTo[List[Device]]
      devices map { devices =>
        Update(devices)
      } pipeTo newStreamActor
    case UpdateForUser(user, data) =>
      lookupBus.publish(MsgEnvelope("user="+user, Update(data)))
  }

}
