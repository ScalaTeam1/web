package actors

import actors.PredictActor.{PredictStep, ProcessStep}
import akka.actor.Actor
import com.google.inject.Inject
import services.PredictorService

object PredictActor {
  case class PredictStep(uuid: String, path: String)

  case class ProcessStep(uuid: String, path: String)

  case class UpdateStep(uuid: String)
}

class PredictActor @Inject()(service: PredictorService) extends Actor {
  def receive: Receive = {
    case ProcessStep(uuid, path) => service.process(uuid, path)
    case PredictStep(uuid, path) => service.predict(uuid, path)
    // case UpdateStep(uuid) => service.update(uuid)
    case _ => println("error")
  }

}
