package controllers

import com.google.gson.Gson
import com.google.inject.Singleton
import com.neu.edu.FlightPricePrediction.pojo.Flight
import play.api.Logger
import play.api.mvc._
import services.PredictorService
import utils.FileUtil

import java.nio.file.Paths
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext

@Singleton
class PredictController @Inject()(predictor: PredictorService, cc: MessagesControllerComponents)
                                 (implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

  def predictSingleFlight: Action[AnyContent] = Action { request: Request[AnyContent] =>
    val json = request.body.asJson
    json match {
      case Some(x) =>
        val gson = new Gson
        val flight = gson.fromJson(x.toString(), classOf[Flight])
        val df = predictor.predict(flight)
        Ok(predictor.dfToJson(df))
      case None => BadRequest("Empty")
    }
  }

  def predict = Action(parse.temporaryFile) { implicit request =>
    val string = UUID.randomUUID().toString
    val path = request.body.moveTo(Paths.get(s"${FileUtil.getUploadPath(string)}input.csv"), replace = true)
    val df = predictor.predict(path.toAbsolutePath.toString)
    Ok(predictor.dfToJson(df))
  }

}
