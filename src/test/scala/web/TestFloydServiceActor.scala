import akka.testkit.TestActorRef
import io.floyd.web.FloydServiceActor
import org.scalatest.{Matchers, FlatSpec}
import spray.testkit.ScalatestRouteTest

class TestFloydServiceActor extends FlatSpec with Matchers with ScalatestRouteTest {

  val route = TestActorRef[FloydServiceActor].underlyingActor.route

  "FloydServiceActor" should "return ping calls" in {
    Get("/ping") ~> route ~> check {
      responseAs[String] should be ("PONG")
    }
  }

}
