import akka.testkit.TestActorRef
import spray.http.{SetRequestTimeout, ChunkedResponseStart, HttpEntity, HttpResponse, MessageChunk}
import spray.can.Http
import spray.http.ContentTypes.`application/json`
import scala.concurrent.duration._

import io.floyd.StreamerActor
import io.floyd.StartStream
import io.floyd.Update


class TestStreamerActor extends BaseUnitTestActor {

  "StreamerActor" should "send a SetRequestTimeout and a Start Request to its client" in {
    val actorRef = TestActorRef(StreamerActor.props(self))
    actorRef ! StartStream()
    expectMsg(SetRequestTimeout(10 minutes))
    expectMsg(ChunkedResponseStart(
        HttpResponse(entity = HttpEntity(`application/json`, s"""{ data:\"start\" }\n"""))))
    expectNoMsg()
  }

  "StreamerActor" should "send JSON updates to its client" in {
    val actorRef = TestActorRef(StreamerActor.props(self))
    actorRef ! Update("test")
    expectMsg(MessageChunk(s"""{ data:"test" }\n"""))
    expectNoMsg()
  }
}