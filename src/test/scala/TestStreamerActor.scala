import org.scalatest._
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import spray.http.SetRequestTimeout
import scala.concurrent.duration._

import io.floyd.StreamerActor
import io.floyd.StartStream


class StreamerActorSpec extends TestKit(ActorSystem()) with ImplicitSender with FlatSpecLike with Matchers  {

  "StreamerActor" should "send a SetRequestTimeout to registered actor" in {
    val actorRef = TestActorRef(StreamerActor.props(self)) 
    actorRef ! StartStream()
    expectMsg(SetRequestTimeout(10 minutes))
  }
}