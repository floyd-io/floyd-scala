import akka.testkit.TestActorRef

import scala.concurrent.duration._

import io.floyd.{LoginUser, TokenAuthActor}

class TestTokenAuthActor extends InsertOnStartupEmail {
  "TokenAuthActor" should "give valid UUID for valid user" in {
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! LoginUser("testEmail@yahoo.com", "password")
    expectMsgPF(5 seconds) {
      case auth:String =>
        auth should fullyMatch regex "(\\w{8}(-\\w{4}){3}-\\w{12}?)".r
    }
  }
}
