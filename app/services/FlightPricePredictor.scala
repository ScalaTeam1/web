package services

import com.neu.edu.FlightPricePrediction.pojo.Flight
import ml.dmlc.xgboost4j.scala.spark.XGBoostRegressionModel
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.{DataFrame, Dataset}

import scala.util.Try

/**
 * @author Caspar
 * @date 2022/4/4 23:20
 */
class FlightPricePredictor(modelId: String, modelPath: String, preprocessorPath: String) {
  val id: String = modelId
  val path: String = modelPath
  val pPath: String = preprocessorPath
  //  val dataPath: String = predictPath


  val model: XGBoostRegressionModel = loadModel(path)
  val preprocessor: PipelineModel = loadPreprocessModel(pPath)

  private def loadModel(path: String) = XGBoostRegressionModel.load(path)

  private def loadPreprocessModel(path: String): PipelineModel = PipelineModel.load(path)

  def predict(data: Try[Dataset[Flight]]): Try[DataFrame] = {
    for (ds <- data) yield model.transform(preprocessor.transform(ds))
  }

}
