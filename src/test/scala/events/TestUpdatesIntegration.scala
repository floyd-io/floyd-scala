import io.floyd.events.{Update, EventsActor}

import akka.actor.Props
import akka.testkit.TestProbe
import spray.http.MessageChunk

class TestUpdatesIntegration extends BaseUnitTestActor with UpdateHttpDataMatcher {
  "An update event sent" should "update several streams" in {
    val allEventsActor = system.actorOf(Props[EventsActor])
    val listOfClients = (1 to 5) map { _ => new TestProbe(system) }
    listOfClients foreach {
      (testProbe: TestProbe) =>
      allEventsActor ! testProbe.ref
      testProbe.receiveN(2)
    }

    allEventsActor ! Update(Map("data" -> "multipleUpdate"))
    listOfClients foreach {
      _.expectMsgPF() {
        case MessageChunk(data, extension) =>
          jsonShouldBe(data, Map("data" -> "multipleUpdate"))
      }
    }
  }
}


