package models

import cn.playscala.mongo.annotations.Entity

@Entity("task")
case class Task(uuid: String, state: Int, inputPath: String, outputPath: String, lines: Long)

object Task {

}

