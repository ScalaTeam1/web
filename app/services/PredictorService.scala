package services

import com.google.inject.{Inject, Singleton}
import com.neu.edu.FlightPricePrediction.pojo.{Flight, FlightReader, IterableFlightReader}
import com.neu.edu.FlightPricePrediction.predictor.FightPricePredictor
import config.ContextHolder
import org.apache.spark.sql
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

}
