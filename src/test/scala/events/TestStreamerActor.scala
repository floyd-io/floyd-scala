import io.floyd.events.{StreamerActor, Update, StartStream}

import akka.testkit.TestActorRef
import spray.http.{SetRequestTimeout, ChunkedResponseStart,
  HttpResponse, MessageChunk}
  
import scala.concurrent.duration._

class TestStreamerActor extends BaseUnitTestActor with UpdateHttpDataMatcher {

  "StreamerActor" should "send a SetRequestTimeout and a Start Request to its client" in {
    val actorRef = TestActorRef(StreamerActor.props(self))
    actorRef ! StartStream()
    expectMsg(SetRequestTimeout(10 minutes))
    expectMsgPF() {
      case ChunkedResponseStart(HttpResponse(_, httpEntity, _, _)) =>
        jsonShouldBe(httpEntity.data, "start")
    }
  }

  it should "send JSON updates to its client" in {
    val actorRef = TestActorRef(StreamerActor.props(self))
    actorRef ! Update("test")
    expectMsgChunk("test")
  }
}