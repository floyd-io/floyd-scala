import io.floyd.actors.{DeviceNotRegistered, DeviceRegistered, RegisterDevice,
  DeviceRegisterActor}
import io.floyd.events.UpdateForUser
import io.floyd.db.ReactiveConnection

import akka.actor.Props
import reactivemongo.bson.BSONDocument

import org.scalatest.concurrent.ScalaFutures
import akka.testkit.TestActorRef
import akka.testkit.TestProbe

import scala.concurrent.ExecutionContext.Implicits.global

class TestDeviceRegisterActor extends BaseUnitTestActor with ScalaFutures {
  val devices = ReactiveConnection.db("devices")

  "DeviceRegisterActor" should "insert a device in the DB and send an update to the stream" in {
    val testprobe = TestProbe()
    val deviceRegisterActor = TestActorRef(Props(classOf[DeviceRegisterActor], testprobe.ref))
    val registerDeviceMsg =
      RegisterDevice("111", "020202", "device from tests integration", "user1@yahoo.com", "smartBulb")

    deviceRegisterActor ! registerDeviceMsg
    expectMsg(DeviceRegistered)

    val update = testprobe.expectMsgClass(classOf[UpdateForUser])
    update.user should be ("user1@yahoo.com")
    update.data should be (s"device registered = $registerDeviceMsg")

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

  "DeviceRegisterActor" should "give an error when a user is in the DB" in {
    val testProbe = TestProbe()
    val deviceRegister = TestActorRef(Props(classOf[DeviceRegisterActor], testProbe.ref))
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
