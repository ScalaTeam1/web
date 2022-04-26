package controllers

import akka.actor.ActorSystem
import cn.playscala.mongo.Mongo
import com.google.gson.Gson
import models.Model
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._

import java.time.LocalDateTime
import java.util.UUID
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(mongo: Mongo, system: ActorSystem, config: Configuration, cc: ControllerComponents) extends AbstractController(cc) {
  //  val helloActor = system.actorOf(PredictActor.props, "hello-actor")

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  //  def index = Action {
  //    val modelId = config.get[String]("predictor.model_id")
  //    Ok(modelId)
  //  }

  val gson = new Gson

  def index = Action.async {
    val model = Model(UUID.randomUUID().toString, "", "", 1.0, LocalDateTime.now)
    mongo.insertOne[Model](model)

    val value = mongo.find[Model]().sort({
      Json.obj("datetime" -> -1)
    }).first
    value.map { model => {
      Ok(gson.toJson(model))
    }
    }
  }

  def hello = Action {
    Ok(System.getProperty("user.dir"))
  }

}
