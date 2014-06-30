package io.floyd.actors

import io.floyd.db.ReactiveConnection

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import reactivemongo.bson.BSONDocument


case class RegisterDevice(deviceId: String, serialNumber: String, description: String, userId: String)
case class DeviceRegistered()
case class DeviceNotRegistered()

class DeviceRegisterActor(userActor: ActorRef) extends Actor with ActorLogging {
  val collection = ReactiveConnection.db.apply("devices")

  import context.dispatcher

  def receive = {
    case registerDevice: RegisterDevice =>
      val device = BSONDocument(
        "_id" -> registerDevice.deviceId,
        "serial_number" -> registerDevice.serialNumber,
        "description" -> registerDevice.description,
        "user_id" -> registerDevice.userId
      )

      collection.insert(device) map { lastError =>
        userActor ! UpdateForUser(registerDevice.userId,
          s"device registered = ${registerDevice}"
        )
        DeviceRegistered
      } recover { case ex =>
        DeviceNotRegistered
      } pipeTo sender
  }
}
