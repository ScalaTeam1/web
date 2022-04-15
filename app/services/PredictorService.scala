package services

import com.google.inject.{Inject, Singleton}
import com.neu.edu.FlightPricePrediction.pojo.{Flight, FlightReader, IterableFlightReader}
import org.apache.spark.sql
import play.api.Configuration
import utils.FileUtil._

import scala.util.Try

@Singleton
class PredictorService @Inject()(config: Configuration) {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  private val modelId = config.get[String]("predictor.model_id")
  private val preprocessorPath = getPreprocessModelPath("test", modelId)
  private val modelPath = getModelPath("test", modelId)


  /**
   * @param flightDataPath local path
   * @return
   */
  def predict(flightDataPath: String): Try[sql.DataFrame] = {
    val input = FlightReader(flightDataPath)
    val predictor = new FlightPricePredictor(modelId, modelPath, preprocessorPath)
    val output = predictor.predict(input.dy)
    output
  }

  def predict(flight: Flight): Try[sql.DataFrame] = {
    val input = IterableFlightReader(Seq(flight))
    val predictor = new FlightPricePredictor(modelId, modelPath, preprocessorPath)
    val output = predictor.predict(Try.apply(input.dy))
    output
  }

  private val model = FightPricePredictor.loadModel("./tmp/e86f1ef3-c24a-40bd-8220-bebf81ff85b1/best_model")
  private val preprocesspr = FightPricePredictor.loadPreprocessModel("./tmp/e86f1ef3-c24a-40bd-8220-bebf81ff85b1/preprocess_model")

  private val predictor: Predictor[Flight] =  new FightPricePredictor("123", model, preprocesspr)
  val brPredictor = sc.broadcast(predictor)

  def predict(data: Try[Dataset[Flight]]) = {
    brPredictor.value.predict(data)
  }

}
