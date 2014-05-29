import akka.testkit.TestActorRef
import spray.http.{SetRequestTimeout, ChunkedResponseStart, HttpEntity, HttpResponse, MessageChunk}
import spray.can.Http
import spray.http.ContentTypes.`application/json`
import scala.concurrent.duration._
import scala.util.parsing.json._
import spray.http.HttpCharsets
import spray.http.HttpData

import io.floyd.StreamerActor
import io.floyd.StartStream
import io.floyd.Update

class TestStreamerActor extends BaseUnitTestActor {

  def expectJsonData(expected:String, actualJson: HttpData) = {
    val jsonString = actualJson.asString(HttpCharsets.`UTF-8`)
    val result = JSON.parseFull(jsonString)
    result match {
      case Some(e) => e.asInstanceOf[Map[String,Any]].get("data") should be (Some(expected))
      case x => 
        throw new Exception("failed parsing of JSON")
    }
  }

  "StreamerActor" should "send a SetRequestTimeout and a Start Request to its client" in {
    val actorRef = TestActorRef(StreamerActor.props(self))
    actorRef ! StartStream()
    expectMsg(SetRequestTimeout(10 minutes))
    expectMsgPF() {
      case ChunkedResponseStart(HttpResponse(_,httpEntity,_,_)) => 
        expectJsonData("start", httpEntity.data)
    }
    expectNoMsg()
  }

  "StreamerActor" should "send JSON updates to its client" in {
    val actorRef = TestActorRef(StreamerActor.props(self))
    actorRef ! Update("test")
    expectMsgPF() {
      case MessageChunk(data, extension) => 
        expectJsonData("test", data)
    }
    expectNoMsg()
  }
}