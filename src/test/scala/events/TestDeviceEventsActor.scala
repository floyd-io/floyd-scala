
import io.floyd.events._
import io.floyd.actors._

import akka.testkit.TestActorRef

class TestDeviceEventsActor extends BaseUnitTestActor with UpdateHttpDataMatcher {
  "DeviceEventsActor" should "should receive messages from lookupbus" in {
    val deviceEventsActor = TestActorRef[DeviceEventsActor]
    deviceEventsActor ! CreateStreamDevice("919", testActor)
    receiveN(2)
    val lookupBus = LookupBusImpl.instance
    lookupBus.publish(MsgEnvelope("device=919", Update(Map("data" -> "updateDevice"))))
    expectMsgChunk(Map("data" -> "updateDevice"))
  }

}
