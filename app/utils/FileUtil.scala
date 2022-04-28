package utils;

import com.neu.edu.FlightPricePrediction.db.MinioOps
import org.zeroturnaround.zip.ZipUtil
import play.api.Logger

import java.io.File
import java.nio.file.Files

object FileUtil {

  private val logger = Logger(this.getClass)

  val rootPath = System.getProperty("user.dir")

  private val separator: String = java.io.File.separator

  def generateFilePath(bucket: String): String = {
    val path = s"$rootPath${separator}tmp$separator$bucket$separator"
    if (!new File(path).exists()) {
      new File(path).mkdirs()
    }
    path
  }

  def getUploadPath(uuid: String): String = {
    val path = s"$rootPath${separator}tmp${separator}upload${separator}$uuid$separator"
    if (!new File(path).exists()) {
      new File(path).mkdirs()
    }
    path
  }


  def generateFileOutputPath(uuid: String): String = s"${getUploadPath(uuid)}output$separator"

  def getModelPath(bucket: String, id: String) = s"${generateUnzipFilePath(bucket, id)}${separator}best_model${separator}"

  def getPreprocessModelPath(bucket: String, id: String) = s"${generateUnzipFilePath(bucket, id)}${separator}preprocess_model${separator}"

  def generateUnzipFilePath(bucket: String, id: String) = s"${generateFilePath(bucket)}unzip$separator$id"

  /**
   *
   * @param bucket
   * @param objectName
   */
  def downloadIfNotExist(bucket: String, objectName: String): Unit = {
    val saveDirPath: String = s"${generateFilePath(bucket)}"
    val zipPath = new File(saveDirPath)
    if (!zipPath.exists()) {
      zipPath.mkdir()
    }
    logger.info(s"downloading $saveDirPath")
    MinioOps.getFile(bucket, objectName, saveDirPath, s"$objectName")
    val unzipPath = generateUnzipFilePath(bucket, objectName.replace(".zip", ""))
    logger.info(s"unzip $unzipPath")
    new File(unzipPath).mkdir()
    ZipUtil.unpack(new File(saveDirPath + s"$objectName"), new File(unzipPath))
  }

  def download(file: File, filename: String): File = {
    val dest = FileUtil.generateFilePath("upload") + filename
    val newFile = new File(dest)
    if (newFile.exists()) {
      newFile.delete()
    }
    Files.move(file.toPath, newFile.toPath)
    newFile
  }


}
