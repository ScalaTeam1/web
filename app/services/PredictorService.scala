package services

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.google.gson.Gson
import com.google.inject.{Inject, Singleton}
import com.neu.edu.FlightPricePrediction.pojo.{Flight, FlightReader, IterableFlightReader}
import com.neu.edu.FlightPricePrediction.predictor.FightPricePredictor
import config.ContextHolder
import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import play.api.Configuration
import utils.FileUtil._

import scala.util.{Failure, Success, Try}

@Singleton
class PredictorService @Inject()(holder: ContextHolder, config: Configuration) {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  private val modelId = config.get[String]("predictor.model_id")
  private val preprocessorPath = getPreprocessModelPath("test", modelId)
  private val modelPath = getModelPath("test", modelId)
  private val predictor = new FightPricePredictor(modelId, FightPricePredictor.loadModel(modelPath), FightPricePredictor.loadPreprocessModel(preprocessorPath))
  private val brPredictor = holder.context.broadcast(predictor)

  def print(msg: String) = {
    println(msg)
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

  private def predict(input: Try[sql.Dataset[Flight]]) = {
    val output = brPredictor.value.predict(input)
    output match {
      case Success(value) => value
      case Failure(exception) => {
        logger.error(exception.getMessage)
        throw new RuntimeException(exception.getMessage)
      }
    }
  }

  def streaming(flightDataPath: String): String = {
    val spark = SparkSession.builder().getOrCreate()
    val kafka = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:") // comma separated list of broker:host
      .option("subscribe", "test") // comma separated list of topics
      .option("startingOffsets", "latest") // read data from the end of the stream
      .load()
    import spark.implicits._
    val df = kafka.as[Flight]
    predict(Try.apply(df))


    val dstream = holder.streamingContext.textFileStream(flightDataPath)
    val output = ""
    dstream.saveAsTextFiles(output)

    //    val df = predict(output)
    val generator = new UUIDGenerator
    val uuid = generator.generateId().toString

    uuid
  }

  def flightsToCsv(flights: sql.DataFrame): String = {
    val output = ""
    flights.write.format("").save(output)
    output
  }

  def dfToArray(df: sql.DataFrame) = {
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
