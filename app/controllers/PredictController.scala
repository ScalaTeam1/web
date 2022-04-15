package controllers

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import com.google.gson.Gson
import com.google.inject.Singleton
import com.neu.edu.FlightPricePrediction.pojo.Flight
import controllers.flight.FormData
import org.apache.spark.sql.DataFrame
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.{Logger, mvc}
import play.core.parsers.Multipart.FileInfo
import services.PredictorService
import utils.FileUtil

import java.io.File
import java.nio.file.{Files, Path}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PredictController @Inject()(predictor: PredictorService, cc: MessagesControllerComponents)
                                 (implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

  def index: mvc.Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index(form))
  }

  val form: Form[FormData] = Form(
    mapping(
      "name" -> text,
      "modelPath" -> text
    )(FormData.apply)(FormData.unapply)
  )

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType, _) =>
      val path: Path = Files.createTempFile("multipartBody", "tempFile")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          logger.info(s"count = $count, status = $status")
          FilePart(partName, filename, contentType, path.toFile)
      }
  }


  def predictSingleFlight: Action[AnyContent] = Action { request: Request[AnyContent] =>
    val json = request.body.asJson
    json match {
      case Some(x) =>
        val gson = new Gson
        val flight = gson.fromJson(x.toString(), classOf[Flight])
        val df = predictor.predict(flight)
        Ok(mapToJson(df))
      case None => BadRequest("Empty")
    }
  }

  private def mapToJson(value: DataFrame) = {
    val rows = value.select("id", "airline", "flight", "sourceCity", "departureTime", "stops", "arrivalTime", "destinationCity", "classType", "duration", "daysLeft", "prediction").collect()
    val flights = rows.map(row => {
      new Flight(row.getAs(0), row.getAs(1), row.getAs(2), row.getAs(3), row.getAs(4), row.getAs(5), row.getAs(6), row.getAs(7), row.getAs(8), row.getAs(9), row.getAs(10), row.getAs[Double](11).toInt)
    })
    val gson = new Gson
    gson.toJson(flights)
  }

  def predict: mvc.Action[MultipartFormData[File]] = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
        logger.info(s"key = $key, filename = $filename, contentType = $contentType, file = $file, fileSize = $fileSize, dispositionType = $dispositionType")
        val data = FileUtil.download(file, filename)
        data
    }
    fileOption match {
      case Some(value) =>
        val df = predictor.predict(value.getAbsolutePath)
        Ok(mapToJson(df))
      case None => BadRequest("No file found")
    }
  }

}
