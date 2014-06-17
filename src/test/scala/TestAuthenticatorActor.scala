import akka.testkit.TestActorRef

import io.floyd.db.ReactiveConnection
import io.floyd.AuthenticatorActor

import reactivemongo.bson.BSONDocument
import spray.routing.authentication.UserPass

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

  "AuthenticatorActor" should "validate correct user in DB with Some(username)" in {
    authenticator ! Some(UserPass("testEmail@yahoo.com", "password"))
    expectMsg(Some("testEmail@yahoo.com"))
  }

  "Authenticator" should "invalidate inexistant user in DB with None" in {
    authenticator ! Some(UserPass("invalidEmail@yahoo.com", "password"))
    expectMsg(None)
  }

  "Authenticator" should "invalidate incorrect password user in DB with None" in {
    authenticator ! Some(UserPass("testEmail@yahoo.com", "passwordIncorrect"))
    expectMsg(None)
  }

  "Authenticator" should "invalidate None if sent with None" in {
    authenticator ! None
    expectMsg(None)
  }

}