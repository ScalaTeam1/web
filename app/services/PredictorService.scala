package services

import actors.PredictActor.ProcessStep
import akka.actor.{ActorRef, ActorSystem}
import cn.playscala.mongo.Mongo
import com.google.gson.Gson
import com.google.inject.{Inject, Singleton}
import com.neu.edu.FlightPricePrediction.db.MinioOps
import com.neu.edu.FlightPricePrediction.pojo.{Flight, FlightReader, IterableFlightReader}
import config.ContextHolder
import models.Task
import org.apache.spark.sql
import org.zeroturnaround.zip.ZipUtil
import play.api.libs.json.Json.obj
import play.api.{Configuration, Logger}
import utils.FileUtil

import java.io.File
import javax.inject.Named
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

@Singleton
class PredictorService @Inject()(mongo: Mongo, @Named("configured-actor") myActor: ActorRef, actorSystem: ActorSystem, holder: ContextHolder, config: Configuration) {

  private val logger = Logger(this.getClass)

  /**
   * insert a task and start to predict
   *
   * @param uuid     uuid of document
   * @param dataPath input datapath
   * @return no return
   */
  def predict(uuid: String, dataPath: String): Unit = {
    val reader = FlightReader(dataPath)
    var task = new Task(uuid, 0, dataPath, "", 0L)
    reader.dy match {
      case Success(value) => task = task.copy(lines = value.count())
      case Failure(exception) => throw new RuntimeException(exception.getMessage)
    }
    mongo.insertOne[Task](task)
    val frame = predict(dataPath)
    val output = flightsToCsv(uuid, frame)
    mongo.updateById[Task](uuid, obj("$set" -> obj("state" -> 1)))
    actorSystem.scheduler.scheduleOnce(0.milliseconds, myActor, ProcessStep(uuid, output))
  }

  def process(uuid: String, dataPath: String): Unit = {
    val output = s"${FileUtil.getUploadPath(uuid)}output.zip"
    ZipUtil.pack(new File(dataPath), new File(output))
    MinioOps.putFile("test", s"${uuid}_output.zip", output) match {
      case Failure(exception) =>
        logger.error(s"[$uuid]: " + exception.getMessage)
      case Success(_) =>
        mongo.updateById[Task](uuid, obj("$set" -> obj("state" -> 2, "outputPath" -> output)))
        logger.info(s"${uuid} job finished")
    }
  }


  /**
   * @param flightDataPath local path
   * @return
   */
  def predict(flightDataPath: String): sql.DataFrame = {
    val input = FlightReader(flightDataPath)
    predict(input.dy)
  }

  def predict(flight: Flight): sql.DataFrame = {
    val input = IterableFlightReader(Seq(flight))
    predict(Try.apply(input.dy))
  }

  def predict(flights: Seq[Flight]): sql.DataFrame = {
    val input = IterableFlightReader(flights)
    predict(Try.apply(input.dy))
  }

  private def predict(input: Try[sql.Dataset[Flight]]) = {

    val output = holder.brPredictor.value.predict(input)
    output match {
      case Success(value) => value
      case Failure(exception) =>
        logger.error(exception.getMessage)
        throw new RuntimeException(exception.getMessage)
    }
  }

  def flightsToCsv(uuid: String, flights: sql.DataFrame): String = {
    val output = FileUtil.generateFileOutputPath(uuid)
    val tmp = flights.select("id", "airline", "flight", "sourceCity", "departureTime", "stops", "arrivalTime", "destinationCity", "classType", "duration", "daysLeft", "prediction")
    tmp.repartition(1).write.format("csv").save(output)
    logger.info(s"prediction finished $uuid")
    output
  }

  def dfToArray(df: sql.DataFrame): Array[Flight] = {
    val rows = df.select("id", "airline", "flight", "sourceCity", "departureTime", "stops", "arrivalTime", "destinationCity", "classType", "duration", "daysLeft", "prediction").collect()
    val flights = rows.map(row => {
      new Flight(row.getAs(0), row.getAs(1), row.getAs(2), row.getAs(3), row.getAs(4), row.getAs(5), row.getAs(6), row.getAs(7), row.getAs(8), row.getAs(9), row.getAs(10), row.getAs[Double](11).toInt)
    })
    flights
  }

  def dfToJson(df: sql.DataFrame): String = {
    val gson = new Gson
    gson.toJson(dfToArray(df))
  }
}
