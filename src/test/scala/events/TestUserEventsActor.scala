import io.floyd.events.{UpdateForUser, StartStreamForUser, UserEventsActor}

import akka.testkit.{TestProbe, TestActorRef}
import spray.http.MessageChunk

class TestUserEventsActor extends BaseUnitTestActor with UpdateHttpDataMatcher {
  "UserEventsActor" should "create one stream for one message start" in {
    val userEventsActor = TestActorRef[UserEventsActor]
    val testprobe1 = TestProbe()
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe1.ref)
    testprobe1.receiveN(4)
  }

  it should "create two actor streams for two streams message start" in {
    val userEventsActor = TestActorRef[UserEventsActor]
    val testprobe1 = TestProbe()
    val testprobe2 = TestProbe()
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe1.ref)
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe2.ref)
    testprobe1.receiveN(4)
    testprobe2.receiveN(4)
  }

  it should "create two streams for different users" in {
    val userEventsActor = TestActorRef[UserEventsActor]
    val testprobe1 = TestProbe()
    val testprobe2 = TestProbe()
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe1.ref)
    userEventsActor ! StartStreamForUser("user2@hotmail.com", testprobe2.ref)
    testprobe1.receiveN(4)
    testprobe2.receiveN(4)
  }

  it should "send update to the correct user" in {
    val userEventsActor = TestActorRef[UserEventsActor]
    val testprobe1 = TestProbe()
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe1.ref)
    testprobe1.receiveN(4)
    userEventsActor ! UpdateForUser("user1@hotmail.com", Map("data" -> "update"))
    testprobe1.expectMsgPF() {
      case MessageChunk(data, extension) =>
        jsonShouldBe(data, "update")
    }
  }
}
