package config

import cn.playscala.mongo.Mongo
import com.google.inject.Inject
import com.neu.edu.FlightPricePrediction.db.MinioOps
import com.neu.edu.FlightPricePrediction.predictor.FightPricePredictor
import io.minio.errors.ErrorResponseException
import models.Model
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.{SparkConf, SparkContext}
import org.zeroturnaround.zip.ZipException
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import utils.FileUtil.{downloadIfNotExist, generateUnzipFilePath, getModelPath, getPreprocessModelPath}

import java.io.File
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ContextHolder @Inject()(config: Configuration, mongo: Mongo, applicationLifecycle: ApplicationLifecycle) {
  val logger: Logger = Logger(this.getClass)

  val conf = new SparkConf
  conf.setAppName("web")
  conf.setMaster("local[*]")
  val context: SparkContext = SparkContext.getOrCreate(conf)
  val defaultBucket: String = config.get[String]("bucket")

  applicationLifecycle.addStopHook { () =>
    Future.successful {
      context.stop()
    }
  }

  private val modelId: String = {
    val value = mongo.find[Model]().sort({
      Json.obj("datetime" -> -1, "score" -> -1)
    }).list()
    Try(Await.result(value, 10 seconds)) match {
      case Success(list) =>
        val uuid = list.filter(model => {
          try {
            val stat = MinioOps.minioClient.statObject(defaultBucket, model.uuid + ".zip")
            stat.etag().nonEmpty
          } catch {
            case _: ZipException => logger.warn(s"pass empty file ${model.uuid}"); false;
            case _: ErrorResponseException => logger.warn(s"pass error response ${model.uuid}"); false;
            case e: Exception => logger.warn(e.getMessage); false;
          }
        }).map(model => model.uuid)
        uuid match {
          case Nil => config.get[String]("predictor.model_id")
          case _ => uuid.head
        }
      case Failure(_) =>
        val tmp = config.get[String]("predictor.model_id")
        logger.info(s"download model ${tmp}")
        tmp
      case _ =>
        logger.error("Very Strange")
        System.exit(0)
        ""
    }
  }


  val _ = try {
    val unzipPath = generateUnzipFilePath(defaultBucket, modelId)
    val file = new File(unzipPath)
    if (!file.exists()) {
      downloadIfNotExist(defaultBucket, modelId + ".zip")
    }
  } catch {
    case _: ZipException => System.exit(-1)
  }
  private val preprocessorPath = getPreprocessModelPath(defaultBucket, modelId)
  private val modelPath = getModelPath(defaultBucket, modelId)
  private val predictor = new FightPricePredictor(modelId, FightPricePredictor.loadModel(modelPath), FightPricePredictor.loadPreprocessModel(preprocessorPath))
  val brPredictor: Broadcast[FightPricePredictor] = context.broadcast(predictor)


}
