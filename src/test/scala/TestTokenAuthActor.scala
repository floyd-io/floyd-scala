import io.floyd.actors.TokenAuthActor

import akka.testkit.TestActorRef
import spray.routing.authentication.UserPass

class TestTokenAuthActor extends BaseUnitTestActor with CreateUser {
  "TokenAuthActor" should "give valid UUID for valid user" in withUser { user =>
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass(user, "password")
    val authToken = expectMsgClass(classOf[String])
    authToken should fullyMatch regex "(\\w{8}(-\\w{4}){3}-\\w{12}?)".r
  }

  "TokenAuthActor" should "give valid same UUID for same user" in withUser { user =>
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass(user, "password")
    val authToken = expectMsgClass(classOf[String])
    tokenAuthActor ! UserPass(user, "password")
    expectMsg(authToken)
  }

  "TokenAuthActor" should "give Exception for invalid user" in withUser { user =>
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass("invalidEmail@yahoo.com", "password")
    val authToken = expectMsgClass(classOf[akka.actor.Status.Failure])
    authToken.cause.getMessage() should be ("invalid user")
  }
}
