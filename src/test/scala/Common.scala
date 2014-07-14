import io.floyd.db.ReactiveConnection

import org.scalatest.Matchers
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import spray.http.{MessageChunk, HttpCharsets, HttpData}
import akka.testkit.TestKitBase

import scala.concurrent.Await
import scala.util.parsing.json.JSON
import scala.concurrent.duration._

import java.util.UUID.randomUUID

trait CreateUser {
  import concurrent.ExecutionContext.Implicits.global

  def withUser(testcode: (String, String) => Any) = {
    val username = "test_" + randomUUID.toString + "@yahoo.com"
    val id = BSONObjectID.generate
    val document = BSONDocument(
      "_id" -> id,
      "username" -> username,
      "password" -> "password")
    try {
      val future = ReactiveConnection.db("users").insert(document)
      Await.result(future, 5 seconds)
      testcode(username, id stringify)
    }
    finally {
      val deleteDevices = ReactiveConnection.db("devices").remove(BSONDocument(
        "user_id"-> id
      ))
      Await.result(deleteDevices, 5 seconds)
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
