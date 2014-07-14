package io.floyd.db

import io.floyd.events.{MsgEnvelope, LookupBusImpl, RegisterListener}

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import reactivemongo.bson.BSONDocument


case class RegisterDevice(deviceId: String, serialNumber: String,
                          description: String, userId: String,
                          typeOfDevice: String
                           )
case class DeviceRegistered()
case class DeviceNotRegistered()

class DeviceRegisterActor extends Actor with ActorLogging {
  val collection = ReactiveConnection.db.apply("devices")

  import context.dispatcher

  val lookupbus = LookupBusImpl.instance

  def receive = {
    case registerDevice: RegisterDevice =>
      val device = BSONDocument(
        "_id" -> registerDevice.deviceId,
        "serial_number" -> registerDevice.serialNumber,
        "description" -> registerDevice.description,
        "user_id" -> registerDevice.userId,
        "type_of_device" -> registerDevice.typeOfDevice
      )

      collection.insert(device) map { lastError =>
        val register = RegisterListener("device="+registerDevice.deviceId)
        lookupbus.publish(MsgEnvelope("user="+ registerDevice.userId, register))
        DeviceRegistered
      } recover { case ex =>
        DeviceNotRegistered
      } pipeTo sender
  }
}
