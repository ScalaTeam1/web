package controllers

import akka.actor.ActorSystem
import cn.playscala.mongo.Mongo
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.google.gson.Gson
import models.Task
import play.api.libs.Files
import play.api.mvc._
import services.PredictorService
import utils.FileUtil

import java.nio.file.Paths
import javax.inject._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class AsyncController @Inject()(mongo: Mongo, service: PredictorService,
                                cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def upload = Action(parse.temporaryFile) { request =>
    request.body.moveTo(Paths.get("/tmp/picture/uploaded"), replace = true)
    Ok("File uploaded")
  }

  private val root = "/Users/arronshentu/Downloads/web/tmp/upload";

  def message: Action[Files.TemporaryFile] = Action(parse.temporaryFile) { request =>
    val uuid = new UUIDGenerator()
    val string = uuid.generateId().toString
    val path = request.body.moveTo(Paths.get(s"${FileUtil.getUploadPath(string)}input.csv"), replace = true)
    service.predict(string, path.toAbsolutePath.toString)
    Ok(string)
  }

  def get(id: String): Action[AnyContent] = Action {
    val value = new Gson
    val future = mongo.findById[Task](id)
    Await.result(
      future.map {
        case Some(t) => Ok(value.toJson(t))
      }, 2000.milliseconds)

  }

}
