package controllers

import akka.Done
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import com.google.gson.Gson
import com.google.inject.Singleton
import com.neu.edu.FlightPricePrediction.pojo.Flight
import controllers.flight.FormData
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
import scala.util.Success

@Singleton
class PredictController @Inject()(predictor: PredictorService, cc: MessagesControllerComponents)
                                 (implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

  def index: mvc.Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index(form))
  }

  val form: Form[FormData] = Form(
    mapping("name" -> text)(FormData.apply)(FormData.unapply)
  )

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType, _) =>
      val path: Path = Files.createTempFile("multipartBody", "tempFile")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, Success(Done)) =>
          logger.info(s"count = $count")
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
        Ok(predictor.dfToJson(df))
      case None => BadRequest("Empty")
    }
  }

  def predict: mvc.Action[MultipartFormData[File]] = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = getFileBody(request)
    fileOption match {
      case Some(value) =>
        val df = predictor.predict(value.getAbsolutePath)
        Ok(predictor.dfToJson(df))
      case None => BadRequest("No file found")
    }
  }

  def streaming: mvc.Action[MultipartFormData[File]] = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = getFileBody(request)
    fileOption match {
      case Some(value) =>
        val uuid = predictor.streaming(value.getAbsolutePath)
        val gson = new Gson
        Ok(gson.toJson(uuid))
      case None => BadRequest("No file found")
    }
  }

  private def getFileBody(request: MessagesRequest[MultipartFormData[File]]) = {
    request.body.file("name").map {
      case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
        logger.info(s"key = $key, filename = $filename, contentType = $contentType, file = $file, fileSize = $fileSize, dispositionType = $dispositionType")
        val data = FileUtil.download(file, filename)
        data
      case _ => throw new RuntimeException("")
    }
  }

}
