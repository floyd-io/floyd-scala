import io.floyd.db.ReactiveConnection
import org.scalatest.{Suite, Matchers, BeforeAndAfterAll}
import reactivemongo.bson.BSONDocument
import spray.http.{HttpCharsets, HttpData}

import scala.concurrent.Await
import scala.util.parsing.json.JSON
import scala.concurrent.duration._

trait InsertOnStartupEmail extends BaseUnitTestActor with BeforeAndAfterAll {
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
