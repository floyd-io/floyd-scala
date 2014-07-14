import io.floyd.db.{DeviceNotRegistered, DeviceRegistered, RegisterDevice,
  DeviceRegisterActor}
import io.floyd.events.{RegisterListener, LookupBusImpl}
import io.floyd.db.ReactiveConnection

import akka.actor.Props
import reactivemongo.bson.BSONDocument

import org.scalatest.concurrent.ScalaFutures
import akka.testkit.TestActorRef
import akka.testkit.TestProbe

import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID.randomUUID

class TestDeviceRegisterActor extends BaseUnitTestActor with ScalaFutures with CreateUser{
  val devices = ReactiveConnection.db("devices")
  val lookupbus = LookupBusImpl.instance

  "DeviceRegisterActor" should
    "insert a device in the DB and send an update to the stream" in withUser { (user, userId) =>

    val testprobe = TestProbe()
    lookupbus.subscribe(testprobe.ref, "user="+userId)
    val deviceRegisterActor = TestActorRef[DeviceRegisterActor]

    val idDevice = randomUUID.toString
    val registerDeviceMsg =
      RegisterDevice(idDevice, "020202", "device from tests integration", userId, "smartBulb")

    deviceRegisterActor ! registerDeviceMsg
    expectMsg(DeviceRegistered)

    val update = testprobe.expectMsgClass(classOf[RegisterListener])
    update.selector should be (s"device=${idDevice}")

    val futureList = devices.find(BSONDocument("_id" -> idDevice)).
      cursor[BSONDocument].collect[List]()

    whenReady(futureList) { devices =>
      devices should have length 1
      val device = devices.head
      device.getAs[String]("_id") should be (Some(idDevice))
      device.getAs[String]("serial_number") should be (Some("020202"))
      device.getAs[String]("description") should be (Some("device from tests integration"))
      device.getAs[String]("user_id") should be (Some(userId))
      device.getAs[String]("type_of_device") should be (Some("smartBulb"))
    }
  }

  "DeviceRegisterActor" should
    "give an error when a device is registered twice" in withUser { (user, userId) =>

    val deviceRegister =  TestActorRef[DeviceRegisterActor]
    val idDevice = randomUUID.toString

    val registerDevice = RegisterDevice(idDevice,"020331","device2 from tests integration", userId, "smartBulb")
    deviceRegister ! registerDevice
    expectMsg(DeviceRegistered)
    deviceRegister ! registerDevice
    expectMsg(DeviceNotRegistered)
  }

}
