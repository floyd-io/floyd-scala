
import io.floyd.AuthenticatorActor

import akka.testkit.TestActorRef
import spray.routing.authentication.UserPass

class TestAuthenticatorActor extends InsertOnStartupEmail {
  val authenticator = TestActorRef[AuthenticatorActor]

  "AuthenticatorActor" should "validate correct user in DB with Some(username)" in {
    authenticator ! UserPass("testEmail@yahoo.com", "password")
    expectMsg(Some("testEmail@yahoo.com"))
  }

  "Authenticator" should "invalidate inexistant user in DB with None" in {
    authenticator ! UserPass("invalidEmail@yahoo.com", "password")
    expectMsg(None)
  }

  "Authenticator" should "invalidate incorrect password user in DB with None" in {
    authenticator ! UserPass("testEmail@yahoo.com", "passwordIncorrect")
    expectMsg(None)
  }

}