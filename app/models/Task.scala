package models

import cn.playscala.mongo.annotations.Entity

@Entity("tasks")
case class Task(_id: String, state: Int, inputPath: String, outputPath: String, lines: Long)

case class TaskVo(_id: String, state: Int, lines: Long)

object Task {

  val COMPLETE = 2;
  val NEW = 0;
  val PROCESSING = 1;

}
