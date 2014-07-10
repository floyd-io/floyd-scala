package io.floyd.actors

import io.floyd.db.ReactiveConnection

import reactivemongo.bson.BSONDocument
import akka.actor.Actor
import akka.pattern.pipe

import scala.concurrent.Future

case class Device(id: String, serialNumber: String, description: String,
                  typeOfDevice: String)

class DevicesActor extends Actor {
  val devices = ReactiveConnection.db.collection("devices")

  import context.dispatcher

  def receive = {
    case userId:String =>
      val query = BSONDocument("user_id" -> userId)
      val list: Future[List[BSONDocument]] =
        devices.find(query).
          cursor[BSONDocument].collect[List]()
      list map { devicesDB =>
        devicesDB map { device =>
          Device(device.getAs[String]("_id").get,
            device.getAs[String]("serial_number").get,
            device.getAs[String]("description").get,
            device.getAs[String]("type_of_device").get)
        }
      } pipeTo sender
  }
}
