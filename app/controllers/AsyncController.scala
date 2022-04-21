package controllers

import actors.PredictActor.PredictStep
import akka.actor.{ActorRef, ActorSystem}
import cn.playscala.mongo.Mongo
import com.google.gson.Gson
import com.neu.edu.FlightPricePrediction.db.MinioOps
import models.Task
import play.api.Logger
import play.api.libs.Files
import play.api.mvc._
import utils.FileUtil

import java.nio.file.Paths
import java.util.UUID
import javax.inject._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class AsyncController @Inject()(mongo: Mongo,
                                actorSystem: ActorSystem,
                                @Named("configured-actor") myActor: ActorRef,
                                cc: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  private val logger = Logger(this.getClass)

  def predict: Action[Files.TemporaryFile] = Action(parse.temporaryFile) { request =>
    val string = UUID.randomUUID().toString
    val path = request.body.moveTo(Paths.get(s"${FileUtil.getUploadPath(string)}input.csv"), replace = true)
    actorSystem.scheduler.scheduleOnce(0.milliseconds, myActor, PredictStep(string, path.toAbsolutePath.toString))
    Ok(string)
  }

  val gson = new Gson

  def get(id: String): Action[AnyContent] = Action {

    val future = mongo.findById[Task](id)
    Await.result(
      future.map {
        case Some(t) =>
          Ok(gson.toJson(t)).as("application/json")
        case _ => BadRequest("Not found")
      }, 2000.milliseconds)
  }

  def download(id: String): Action[AnyContent] = Action {
    val task = Await.result(
      mongo.findById[Task](id).map {
        case Some(t) => t
        case _ => throw new RuntimeException("")
      }
      , 2000.milliseconds)
    task.state match {
      case 2 =>
        val value = FileUtil.generateFileOutputPath(id)
        MinioOps.getFile("test", s"${id}_output.zip", value, "output.zip")
        Ok.sendFile(new java.io.File(s"${value}output.zip"))
      case _ => BadRequest("not done")
    }
  }

}
