package utils;

import com.neu.edu.FlightPricePrediction.db.MinioOps
import org.zeroturnaround.zip.ZipUtil

import java.io.File
import java.nio.file.Files

object FileUtil {
  // todo

  private val rootPath = System.getProperty("user.dir")

  private val separator: String = java.io.File.separator

  // rootPath/tmp
  // rootPath/tmp/*.zip
  // rootPath/tmp/unzip/uuid/best_model/data/XGBoostRegressionModel
  // rootPath/tmp/uuid/upload
  // rootPath/tmp/uuid/output

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

  def generateZipFilePath(bucket: String, id: String) = s"${generateFilePath(bucket)}$id.zip"

  def getModelPath(bucket: String, id: String) = s"${generateUnzipFilePath(bucket, id)}${separator}best_model${separator}"

  def getPreprocessModelPath(bucket: String, id: String) = s"${generateUnzipFilePath(bucket, id)}${separator}preprocess_model${separator}"

  def generateUnzipFilePath(bucket: String, id: String) = s"${generateFilePath(bucket)}unzip$separator$id"

  def downloadIfNotExist(bucket: String, id: String): Unit = {
    // uuid 去下载
    val saveDirPath: String = s"${generateFilePath(bucket)}"
    val zipPath = new File(saveDirPath)
    if (!zipPath.exists()) {
      zipPath.mkdir()
    }

    println(s"downloading $saveDirPath")
    MinioOps.getFile(bucket, id, saveDirPath, s"$id.zip")
    val unzipPath = generateUnzipFilePath(bucket, id)
    println(s"unzip $unzipPath")
    new File(unzipPath).mkdir()

    ZipUtil.unpack(new File(saveDirPath + s"$id.zip"), new File(unzipPath))
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
