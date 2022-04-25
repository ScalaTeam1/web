package config

import com.google.inject.Inject
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

class ContextHolder @Inject()(applicationLifecycle: ApplicationLifecycle) {

  val conf = new SparkConf
  conf.setAppName("web")
  conf.setMaster("local[*]")
  val context: SparkContext = SparkContext.getOrCreate(conf)

  val streamingContext = new StreamingContext(context, Seconds(1))

  applicationLifecycle.addStopHook { () =>
    Future.successful {
      streamingContext.stop(true)
    }
  }

}
