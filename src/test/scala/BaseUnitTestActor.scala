import org.scalatest._
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.actor.ActorSystem

import spray.http.{HttpCharsets, HttpData}

import scala.util.parsing.json.JSON

abstract class BaseUnitTestActor extends TestKit(ActorSystem()) with ImplicitSender 
  with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    shutdown(verifySystemShutdown = true)
  }
}
