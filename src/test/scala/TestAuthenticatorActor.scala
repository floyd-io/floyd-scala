import akka.testkit.TestActorRef

import io.floyd.{InvalidUser, ValidUser, User, AuthenticatorActor}

class TestAuthenticatorActor extends BaseUnitTestActor {
  val authenticator = TestActorRef[AuthenticatorActor]

  "AuthenticatorActor" should "validate correct user in DB" in {
    authenticator ! User("testEmail@yahoo.com", "password")
    expectMsg(ValidUser)
  }

  "Authenticator" should "invalidate inexistant user in DB" in {
    authenticator ! User("invalidEmail@yahoo.com", "password")
    expectMsg(InvalidUser)
  }

  "Authenticator" should "invalidate incorrect password user in DB" in {
    authenticator ! User("testEmail@yahoo.com", "passwordIncorrect")
    expectMsg(InvalidUser)
  }

}
