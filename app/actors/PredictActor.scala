package actors

import actors.PredictActor.predict
import akka.actor.Actor
import com.google.inject.Inject
import services.PredictorService

object PredictActor {
  case class predict(uuid: String, path: String)
}

class PredictActor @Inject()(service: PredictorService) extends Actor {
  def receive: Receive = {
    case predict(uuid, path) =>
      service.print(s"$uuid: $path")
      println(s"$uuid: $path")
    case _ => println("error")
  }

}
