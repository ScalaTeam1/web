package controllers

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import com.google.inject.Singleton
import controllers.flight.FormData
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, MultipartFormData}
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

  private def operateOnTempFile(file: File, filename: String) = {
    val size = Files.size(file.toPath)
    logger.info(s"size = ${size}")
    download(file, filename)
    size
  }

  /**
   * Uploads a multipart file as a POST request.
   *
   * @return
   */
  def upload: mvc.Action[MultipartFormData[File]] = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
        logger.info(s"key = $key, filename = $filename, contentType = $contentType, file = $file, fileSize = $fileSize, dispositionType = $dispositionType")
        val data = operateOnTempFile(file, filename)
        data
    }

    Ok(s"file size = ${fileOption.getOrElse("no file")}")
  }

  private def download(file: File, filename: String): File = {
    val dest = FileUtil.generateFilePath("upload") + filename
    logger.info(s"${dest}")
    val newFile = new File(dest)
    if (newFile.exists()) {
      newFile.delete()
    }
    Files.move(file.toPath, newFile.toPath)
    newFile
  }


  def predict: mvc.Action[MultipartFormData[File]] = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
        logger.info(s"key = $key, filename = $filename, contentType = $contentType, file = $file, fileSize = $fileSize, dispositionType = $dispositionType")
        val data = download(file, filename)
        data
    }
    if (fileOption.get.exists()) {
      BadRequest("No file found")
    }
    val triedFrame = predictor.predict(fileOption.get.getAbsolutePath)
    val result = triedFrame.get
    result.show(true)
    Ok(result.collect().mkString("\n"))
  }

}
