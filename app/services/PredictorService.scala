package services

<<<<<<< HEAD
import com.neu.edu.FlightPricePrediction.pojo.Flight
import com.neu.edu.FlightPricePrediction.predictor.{FightPricePredictor, Predictor}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.Dataset
import play.api.{Configuration, Logger}
import play.api.inject.ApplicationLifecycle

import javax.inject._
import scala.util.Try

/**
 * This class demonstrates how to run code when the
 * application starts and stops. It starts a timer when the
 * application starts. When the application stops it prints out how
 * long the application was running for.
 *
 * This class is registered for Guice dependency injection in the
 * [[Module]] class. We want the class to start when the application
 * starts, so it is registered as an "eager singleton". See the code
 * in the [[Module]] class to see how this happens.
 *
 * This class needs to run code when the server stops. It uses the
 * application's [[ApplicationLifecycle]] to register a stop hook.
 */
@Singleton
class PredictorService @Inject()(config: Configuration, appLifecycle: ApplicationLifecycle) {

  private val logger: Logger = Logger(this.getClass)
  val conf = new SparkConf().setAppName("prediction").setMaster("local")
  val sc = new SparkContext(conf)

  private val model = FightPricePredictor.loadModel("./tmp/e86f1ef3-c24a-40bd-8220-bebf81ff85b1/best_model")
  private val preprocesspr = FightPricePredictor.loadPreprocessModel("./tmp/e86f1ef3-c24a-40bd-8220-bebf81ff85b1/preprocess_model")

  private val predictor: Predictor[Flight] =  new FightPricePredictor("123", model, preprocesspr)
  val brPredictor = sc.broadcast(predictor)

  def predict(data: Try[Dataset[Flight]]) = {
    brPredictor.value.predict(data)
  }

}
