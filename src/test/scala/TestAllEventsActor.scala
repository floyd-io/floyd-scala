import org.scalatest._
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.testkit.TestActorRef
import akka.actor.ActorSystem

import io.floyd.AllEventsActor

class AllEventsActorSpec extends TestKit(ActorSystem()) with ImplicitSender with FlatSpecLike with Matchers  {

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