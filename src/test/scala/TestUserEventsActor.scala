import io.floyd.{UpdateForUser, UserEventsActor, StartStreamForUser}

import akka.testkit.{TestProbe, TestActorRef}
import spray.http.MessageChunk

class TestUserEventsActor extends BaseUnitTestActor with UpdateHttpDataMatcher {
  "UserEventsActor" should "create one user for one message start" in {
    val userEventsActor = TestActorRef[UserEventsActor]
    val testprobe1 = TestProbe()
    userEventsActor.children.size should be (0)
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe1.ref)
    userEventsActor.children.size should be (1)
    testprobe1.receiveN(2)
  }

  "UserEventsActor" should "create one user for two streams message start" in {
    val userEventsActor = TestActorRef[UserEventsActor]
    val testprobe1 = TestProbe()
    val testprobe2 = TestProbe()
    userEventsActor.children.size should be (0)
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe1.ref)
    userEventsActor.children.size should be (1)
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe2.ref)
    userEventsActor.children.size should be (1)
    testprobe1.receiveN(2)
    testprobe2.receiveN(2)
  }

  "UserEventsActor" should "create two users for different users" in {
    val userEventsActor = TestActorRef[UserEventsActor]
    val testprobe1 = TestProbe()
    val testprobe2 = TestProbe()
    userEventsActor.children.size should be (0)
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe1.ref)
    userEventsActor.children.size should be (1)
    userEventsActor ! StartStreamForUser("user2@hotmail.com", testprobe2.ref)
    userEventsActor.children.size should be (2)
    testprobe1.receiveN(2)
    testprobe2.receiveN(2)
  }

  "UserEventsActor" should "send update to the correct user" in {
    val userEventsActor = TestActorRef[UserEventsActor]
    val testprobe1 = TestProbe()
    userEventsActor ! StartStreamForUser("user1@hotmail.com", testprobe1.ref)
    testprobe1.receiveN(2)
    userEventsActor ! UpdateForUser("user1@hotmail.com", "update")
    testprobe1.expectMsgPF() {
      case MessageChunk(data, extension) =>
        jsonShouldBe(data, "update")
    }
  }
}
