import io.floyd.actors.{DeviceNotRegistered, DeviceRegistered, RegisterDevice,
  DeviceRegisterActor}
import io.floyd.events.{RegisterListener, LookupBusImpl}
import io.floyd.db.ReactiveConnection

import akka.actor.Props
import reactivemongo.bson.BSONDocument

import org.scalatest.concurrent.ScalaFutures
import akka.testkit.TestActorRef
import akka.testkit.TestProbe

import scala.concurrent.ExecutionContext.Implicits.global

class TestDeviceRegisterActor extends BaseUnitTestActor with ScalaFutures {
  val devices = ReactiveConnection.db("devices")
  val lookupbus = LookupBusImpl.instance

  "DeviceRegisterActor" should "insert a device in the DB and send an update to the stream" in {
    val testprobe = TestProbe()
    lookupbus.subscribe(testprobe.ref, "user=user1@yahoo.com")
    val deviceRegisterActor = TestActorRef[DeviceRegisterActor]
    val registerDeviceMsg =
      RegisterDevice("111", "020202", "device from tests integration", "user1@yahoo.com", "smartBulb")

    deviceRegisterActor ! registerDeviceMsg
    expectMsg(DeviceRegistered)

    val update = testprobe.expectMsgClass(classOf[RegisterListener])
    update.selector should be (s"device=111")

    val futureList = devices.find(BSONDocument("_id" -> "111")).
      cursor[BSONDocument].collect[List]()

    whenReady(futureList) { devices =>
      devices should have length 1
      val device = devices.head
      device.getAs[String]("_id") should be (Some("111"))
      device.getAs[String]("serial_number") should be (Some("020202"))
      device.getAs[String]("description") should be (Some("device from tests integration"))
      device.getAs[String]("user_id") should be (Some("user1@yahoo.com"))
      device.getAs[String]("type_of_device") should be (Some("smartBulb"))
    }
  }

  "DeviceRegisterActor" should "give an error when a device is registered twice" in {
    val deviceRegister =  TestActorRef[DeviceRegisterActor]
    deviceRegister !
      RegisterDevice("112","020331","device2 from tests integration", "user1@yahoo.com", "smartBulb")
    expectMsg(DeviceRegistered)
    deviceRegister !
      RegisterDevice("112","020331","device2 from tests integration", "user1@yahoo.com", "smartBulb")
    expectMsg(DeviceNotRegistered)
  }

  override def afterAll() = {
    super.afterAll()
    import scala.concurrent.ExecutionContext.Implicits.global
    devices.remove(BSONDocument())
  }
}
