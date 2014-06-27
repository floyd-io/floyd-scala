import io.floyd.actors.EventsActor

import akka.testkit.TestActorRef

class TestEventsActor extends BaseUnitTestActor {

  "EventsActor" should "increment value when create a child actor" in {
    val actorRef = TestActorRef[EventsActor]
    val actor = actorRef.underlyingActor
    actorRef ! self
    actor.nextValue.next() should be (2)
    receiveN(2)
  }

  "EventsActor.createNameOfStreamer" should "give several stream consecutive names" in {
    val actorRef = TestActorRef[EventsActor]
    val actor = actorRef.underlyingActor
    actor.createNameOfStreamer should be ("stream1")
    actor.createNameOfStreamer should be ("stream2")
  }

}