package io.floyd.events

import io.floyd.actors.{StartStream, NamedChilds}

import akka.actor.{Actor, ActorRef}

case class CreateStreamDevice(deviceId: String, actor: ActorRef)

class DeviceEventsActor extends NamedChilds {
  def receive = {
    case CreateStreamDevice(deviceId, actor) =>
      val newActor = createChild(actor)
      newActor ! StartStream()
      LookupBusImpl.instance.subscribe(newActor, "device="+deviceId)
  }
}
