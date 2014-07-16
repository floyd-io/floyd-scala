import io.floyd.actors.TokenAuthActor

import akka.testkit.TestActorRef
import spray.routing.authentication.UserPass

class TestTokenAuthActor extends BaseUnitTestActor with CreateUser {
  "TokenAuthActor" should "give valid UUID for valid user" in withUser { (user, id) =>
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass(user, "password")
    val (authToken, id) = expectMsgClass(classOf[Tuple2[String,String]])
    authToken should fullyMatch regex "(\\w{8}(-\\w{4}){3}-\\w{12}?)".r
  }

  it should "give valid another UUID for same user" in withUser { (user, id) =>
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass(user, "password")
    val (authToken, id) = expectMsgClass(classOf[Tuple2[String,String]])
    tokenAuthActor ! UserPass(user, "password")
    val (authToken2, id2) = expectMsgClass(classOf[Tuple2[String,String]])
    authToken should not be (authToken2)
    id should be (id2)
  }

  it should "give Exception for invalid user" in withUser { (user, id) =>
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! UserPass("invalidEmail@yahoo.com", "password")
    val authToken = expectMsgClass(classOf[akka.actor.Status.Failure])
    authToken.cause.getMessage() should be ("invalid user")
  }
}
