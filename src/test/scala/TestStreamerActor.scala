import akka.testkit.TestActorRef
import spray.http.{SetRequestTimeout, ChunkedResponseStart,
  HttpResponse, MessageChunk}
import scala.concurrent.duration._

import io.floyd.StreamerActor
import io.floyd.StartStream
import io.floyd.Update

class TestStreamerActor extends BaseUnitTestActor with UpdateHttpDataMatcher {

  "StreamerActor" should "send a SetRequestTimeout and a Start Request to its client" in {
    val actorRef = TestActorRef(StreamerActor.props(self))
    actorRef ! StartStream()
    expectMsg(SetRequestTimeout(10 minutes))
    expectMsgPF() {
      case ChunkedResponseStart(HttpResponse(_, httpEntity, _, _)) =>
        jsonShouldBe(httpEntity.data, "start")
    }
    expectNoMsg()
  }

  "StreamerActor" should "send JSON updates to its client" in {
    val actorRef = TestActorRef(StreamerActor.props(self))
    actorRef ! Update("test")
    expectMsgPF() {
      case MessageChunk(data, extension) => 
        jsonShouldBe(data, "test")
    }
    expectNoMsg()
  }
}