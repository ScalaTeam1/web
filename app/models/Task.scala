package models

import cn.playscala.mongo.annotations.Entity

@Entity("task")
case class Task(_id: String, state: Int, inputPath: String, outputPath: String, lines: Long)

object Task {

}

