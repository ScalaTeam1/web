import akka.actor.ActorSystem
import cn.playscala.mongo.Mongo
import controllers.HomeController
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Configuration
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import play.api.test.{FakeRequest, Injecting}

class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "Get" should {
    "Success in index" in {
      val controller = new HomeController(inject[Mongo], inject[ActorSystem], inject[Configuration], stubControllerComponents())
      val home = controller.hello.apply(FakeRequest(GET, "/"))
      status(home) mustBe 200
      contentAsString(home) must include(System.getProperty("user.dir"))
    }

    "System.dir" in {
      val controller = new HomeController(inject[Mongo], inject[ActorSystem], inject[Configuration], stubControllerComponents())
      val home = controller.index.apply(FakeRequest(GET, "/"))
      status(home) mustBe 200
    }
  }

}
