import akka.testkit.TestActorRef

import io.floyd.{LoginUser, TokenAuthActor}

class TestTokenAuthActor extends InsertOnStartupEmail {
  "TokenAuthActor" should "give valid UUID for valid user" in {
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! LoginUser("testEmail@yahoo.com", "password")
    val authToken = expectMsgClass(classOf[String])
    authToken should fullyMatch regex "(\\w{8}(-\\w{4}){3}-\\w{12}?)".r
  }

  "TokenAuthActor" should "give valid same UUID for same user" in {
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! LoginUser("testEmail@yahoo.com", "password")
    val authToken = expectMsgClass(classOf[String])
    tokenAuthActor ! LoginUser("testEmail@yahoo.com", "password")
    expectMsg(authToken)
  }

  "TokenAuthActor" should "give Exception for invalid user" in {
    val tokenAuthActor = TestActorRef[TokenAuthActor]
    tokenAuthActor ! LoginUser("invalidEmail@yahoo.com", "password")
    val authToken = expectMsgClass(classOf[akka.actor.Status.Failure])
    authToken.cause.getMessage() should be ("invalid user")
  }
}
