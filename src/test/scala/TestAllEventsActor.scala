import akka.testkit.TestActorRef

import io.floyd.AllEventsActor

class TestAllEventsActor extends BaseUnitTestActor {

  "AllEventsActor" should "increment value when create a child actor" in {
    val actorRef = TestActorRef[AllEventsActor]
    val actor = actorRef.underlyingActor
    actorRef ! self
    actor.nextValue.next() should be (2)
  }

  "AllEventsActor.createNameOfStreamer" should "give several stream consecutive names" in {
    val actorRef = TestActorRef[AllEventsActor]
    val actor = actorRef.underlyingActor
    actor.createNameOfStreamer should be ("stream1")
    actor.createNameOfStreamer should be ("stream2")
  }

}