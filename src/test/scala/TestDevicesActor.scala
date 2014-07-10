import io.floyd.db.ReactiveConnection
import io.floyd.actors.{DevicesActor, Device}

import reactivemongo.bson.BSONDocument
import akka.testkit.TestActorRef

import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.UUID._

class TestDevicesActor extends BaseUnitTestActor with CreateUser {

  import concurrent.ExecutionContext.Implicits.global

  def withDevice(testcode: (String, String) => Any) = {
    withUser { (user, userId) =>
      val deviceId = randomUUID.toString
      val document = BSONDocument(
        "_id" -> deviceId,
        "serial_number" -> "999",
        "description" -> "inserted by tests",
        "type_of_device" -> "smartBulb",
        "user_id" -> userId
      )
      try {
        val result = ReactiveConnection.db("devices").insert(document)
        Await.result(result, 5 seconds)
        testcode(userId, deviceId)
      } finally {
        val result = ReactiveConnection.db("devices").remove(document)
        Await.result(result, 5 seconds)
      }
    }
  }


  "DevicesActor" should "respond with the device states" in withDevice { (userId, deviceId) =>
    val devicesActor = TestActorRef[DevicesActor]
    devicesActor ! userId
    expectMsg(List(Device(deviceId,"999","inserted by tests","smartBulb")))
  }
}
