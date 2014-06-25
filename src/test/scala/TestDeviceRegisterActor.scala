import io.floyd.db.ReactiveConnection
import io.floyd.{RegisterDevice, DeviceRegisterActor, DeviceRegistered, DeviceNotRegistered}

import akka.testkit.TestActorRef
import reactivemongo.bson.BSONDocument

class TestDeviceRegisterActor extends BaseUnitTestActor {
  val devices = ReactiveConnection.db("devices")

  "DeviceRegisterActor" should "insert a device in the DB" in {
    val deviceRegister = TestActorRef[DeviceRegisterActor]
    deviceRegister !
      RegisterDevice("111", "020202", "device from tests integration", "user1@yahoo.com")
    expectMsg(DeviceRegistered)
  }

  "DeviceRegisterActor" should "give an error when a user is in the DB" in {
    val deviceRegister = TestActorRef[DeviceRegisterActor]
    deviceRegister !
      RegisterDevice("112","020331","device2 from tests integration", "user1@yahoo.com")
    expectMsg(DeviceRegistered)
    deviceRegister !
      RegisterDevice("112","020331","device2 from tests integration", "user1@yahoo.com")
    expectMsg(DeviceNotRegistered)
  }

  override def afterAll() = {
    super.afterAll()
    import scala.concurrent.ExecutionContext.Implicits.global
    devices.remove(BSONDocument())
  }
}
