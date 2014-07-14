package io.floyd.db

import spray.routing.AuthorizationFailedRejection
import akka.actor.Actor
import akka.pattern.pipe
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

case class DeviceId(deviceId: String, serialNumber: String)

class DeviceAuthActor extends Actor {
  val collectionDevices = ReactiveConnection.db.apply("devices")

  import context.dispatcher

  def receive = {
    case DeviceId(deviceId, serialNumber) =>
      val query = BSONDocument(
        "_id" -> deviceId,
        "serial_number" -> serialNumber
      )
      val list: Future[List[BSONDocument]] = collectionDevices.find(query).
        cursor[BSONDocument].collect[List]()

      list map {
        case deviceFound :: tail => Right(deviceId)
        case Nil => Left(AuthorizationFailedRejection)
      } pipeTo sender
  }
}
