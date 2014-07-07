
import io.floyd.actors.AuthenticatorActor

import akka.testkit.TestActorRef
import spray.routing.authentication.UserPass

class TestAuthenticatorActor extends BaseUnitTestActor with CreateUser {
  val authenticator = TestActorRef[AuthenticatorActor]

  "AuthenticatorActor" should "validate correct user in DB with Some(username)" in withUser { user =>
    authenticator ! UserPass(user, "password")
    expectMsg(Some(user))
  }

  "Authenticator" should "invalidate inexistant user in DB with None" in withUser { user =>
    authenticator ! UserPass("invalidEmail@yahoo.com", "password")
    expectMsg(None)
  }

  "Authenticator" should "invalidate incorrect password user in DB with None" in withUser { user =>
    authenticator ! UserPass(user, "passwordIncorrect")
    expectMsg(None)
  }

}