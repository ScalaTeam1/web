package models

import cn.playscala.mongo.annotations.Entity

import java.time.LocalDateTime

@Entity("models")
case class Model(uuid: String, pipelineModelPath: String, regressionModelPath: String, score: Double, datetime: LocalDateTime)

object Model {
  val name = "models"
}

