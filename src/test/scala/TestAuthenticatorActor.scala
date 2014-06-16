import akka.testkit.TestActorRef

import io.floyd.db.ReactiveConnection
import io.floyd.{InvalidUser, ValidUser, User, AuthenticatorActor}

import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.duration._

class TestAuthenticatorActor extends BaseUnitTestActor {
  val authenticator = TestActorRef[AuthenticatorActor]

  import concurrent.ExecutionContext.Implicits.global
  val document = BSONDocument(
    "username" -> "testEmail@yahoo.com",
    "password" -> "password")

  override def beforeAll() = {
    super.beforeAll()

    val future = ReactiveConnection.db("users").insert(document)
    Await.result(future, 5 seconds)
  }

  override def afterAll() {
    super.afterAll()

    val future = ReactiveConnection.db("users").remove(document)
    Await.result(future, 5 seconds)
  }

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