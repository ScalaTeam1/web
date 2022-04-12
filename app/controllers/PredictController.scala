package controllers

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import com.google.inject.Singleton
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import utils.FileUtil

import java.io.File
import java.nio.file.{Files, Path}
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

case class FormData(name: String, modelPath: String)

@Singleton
class PredictController @Inject()(config: Configuration, cc: MessagesControllerComponents)
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
    val dest = FileUtil.generateFilePath("upload") + filename
    logger.info(s"${dest}")
    val newFile = new File(dest)
    if (newFile.exists()) {
      newFile.delete()
    }
    Files.move(file.toPath, newFile.toPath)
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

}
