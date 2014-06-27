import io.floyd.actors.TokenAuthActor

import akka.testkit.TestActorRef
import spray.routing.authentication.UserPass

class TestTokenAuthActor extends InsertOnStartupEmail {
  "TokenAuthActor" should "give valid UUID for valid user" in {
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass("testEmail@yahoo.com", "password")
    val authToken = expectMsgClass(classOf[String])
    authToken should fullyMatch regex "(\\w{8}(-\\w{4}){3}-\\w{12}?)".r
  }

  "TokenAuthActor" should "give valid same UUID for same user" in {
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass("testEmail@yahoo.com", "password")
    val authToken = expectMsgClass(classOf[String])
    tokenAuthActor ! UserPass("testEmail@yahoo.com", "password")
    expectMsg(authToken)
  }

  "TokenAuthActor" should "give Exception for invalid user" in {
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass("invalidEmail@yahoo.com", "password")
    val authToken = expectMsgClass(classOf[akka.actor.Status.Failure])
    authToken.cause.getMessage() should be ("invalid user")
  }
}
