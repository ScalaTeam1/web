package controllers

import akka.actor.ActorSystem
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import play.api.Configuration
import play.api.mvc._

import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(system: ActorSystem, config: Configuration, cc: ControllerComponents) extends AbstractController(cc) {
  //  val helloActor = system.actorOf(PredictActor.props, "hello-actor")

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    val modelId = config.get[String]("predictor.model_id")
    Ok(modelId)
  }

  def hello = Action {
    val uuid = new UUIDGenerator()
    Ok(uuid.generateId().toString)

  }

}
