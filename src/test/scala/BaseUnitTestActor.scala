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

trait UpdateHttpDataMatcher extends Matchers {
  def jsonShouldBe(httpData: HttpData, expected:String) = {
    val jsonString = httpData.asString(HttpCharsets.`UTF-8`)
    val result = JSON.parseFull(jsonString)
    result match {
      case Some(e) => e.asInstanceOf[Map[String,Any]].get("data") should be (Some(expected))
      case x => throw new Exception("failed parsing of JSON")
    }
  }
}
