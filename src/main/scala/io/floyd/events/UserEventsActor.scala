package io.floyd.events

import akka.actor.{Actor, ActorRef}

case class StartStreamForUser(user: String, client: ActorRef)
case class UpdateForUser(user:String, data: String)

class UserEventsActor extends Actor with NamedStreamChilds {

  val lookupBus = LookupBusImpl.instance

  def receive = {
    case StartStreamForUser(user, client) =>
      val newStreamActor = createChild(client)
      newStreamActor ! StartStream()
      lookupBus.subscribe(newStreamActor, "user="+user)
    case UpdateForUser(user, data) =>
      lookupBus.publish(MsgEnvelope("user="+user, Update(data)))
  }

}
