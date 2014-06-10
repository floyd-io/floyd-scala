import akka.testkit.TestActorRef

import akka.util.Timeout
import akka.pattern.ask

import io.floyd.{InvalidUser, ValidUser, User, AuthenticatorActor}

import scala.concurrent._
import scala.concurrent.duration._

class TestAuthenticatorActor extends BaseUnitTestActor {
  val authenticator = TestActorRef[AuthenticatorActor]

  implicit val timeout = Timeout(5 seconds)

  def futureShouldBe(future: Future[Any], validUser: Boolean) = {
    val resultFromFuture:Any = Await.result(future, timeout.duration)
    if (validUser)
      resultFromFuture should be (ValidUser)
    else
      resultFromFuture should be (InvalidUser)
  }

  "AuthenticatorActor" should "validate correct user in DB" in {
    val future: Future[Any] = authenticator ? User("testEmail@yahoo.com", "password")
    futureShouldBe(future, true)
  }

  "Authenticator" should "invalidate inexistant user in DB" in {
    val future = authenticator ? User("invalidEmail@yahoo.com", "password")
    futureShouldBe(future, false)
  }

  "Authenticator" should "invalidate incorrect password user in DB" in {
    val future = authenticator ? User("testEmail@yahoo.com", "passwordIncorrect")
    futureShouldBe(future, false)
  }
}
