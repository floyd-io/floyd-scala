
import io.floyd.db.AuthenticatorActor

import akka.testkit.TestActorRef
import spray.routing.authentication.UserPass

class TestAuthenticatorActor extends BaseUnitTestActor with CreateUser {
  val authenticator = TestActorRef[AuthenticatorActor]

  "AuthenticatorActor" should "validate correct user in DB with Some(id)" in withUser { (user, id) =>
    authenticator ! UserPass(user, "password")
    expectMsgClass(classOf[String])
  }

  "Authenticator" should "invalidate inexistant user in DB with None" in withUser { (user, id) =>
    authenticator ! UserPass("invalidEmail@yahoo.com", "password")
    expectMsg(None)
  }

  "Authenticator" should "invalidate incorrect password user in DB with None" in withUser { (user, id) =>
    authenticator ! UserPass(user, "passwordIncorrect")
    expectMsg(None)
  }

}