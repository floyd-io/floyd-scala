import io.floyd.db.ReactiveConnection

import org.scalatest.Matchers
import reactivemongo.bson.BSONDocument
import spray.http.{MessageChunk, HttpCharsets, HttpData}
import akka.testkit.TestKitBase

import scala.concurrent.Await
import scala.util.parsing.json.JSON
import scala.concurrent.duration._

import java.util.UUID.randomUUID

trait CreateUser {
  import concurrent.ExecutionContext.Implicits.global

  def withUser(testcode: String => Any) = {
    val username = "test " + randomUUID.toString + "@yahoo.com"
    val document = BSONDocument(
      "username" -> username,
      "password" -> "password")
    try {
      val future = ReactiveConnection.db("users").insert(document)
      Await.result(future, 5 seconds)
      testcode(username)
    }
    finally {
      val future = ReactiveConnection.db("users").remove(document)
      Await.result(future, 5 seconds)
    }
  }
}


trait UpdateHttpDataMatcher extends Matchers with TestKitBase {
  def jsonShouldBe(httpData: HttpData, expected:String) = {
    val jsonString = httpData.asString(HttpCharsets.`UTF-8`)
    val result = JSON.parseFull(jsonString)
    result match {
      case Some(e) => e.asInstanceOf[Map[String,Any]].get("data") should be (Some(expected))
      case x => throw new Exception("failed parsing of JSON")
    }
  }

  def expectMsgChunk(dataExpected: String) = {
    expectMsgPF() {
      case MessageChunk(data, extension) =>
        jsonShouldBe(data, dataExpected)
    }
  }
}
