import org.scalatest._
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.testkit.TestActorRef
import akka.actor.ActorSystem

abstract class BaseUnitTestActor extends TestKit(ActorSystem()) with ImplicitSender 
  with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}