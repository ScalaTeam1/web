package actors

import akka.actor.{Actor, ActorRef}
import com.google.inject.Inject
import models.WsMsg
import play.api.Logger
import services.PredictorService

import scala.util.{Failure, Success, Try}

/**
* @author Caspar
* @date 2022/4/24 19:54 
*/
object StreamPredictActor {
  private val logger = Logger(this.getClass)
}

class StreamPredictActor @Inject()(service: PredictorService)(actor: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: String => {
      Try {
        service.predict(WsMsg(msg).getFlights)
      } match {
        case Success(r) =>
          actor ! service.dfToJson(r)
        case Failure(exception) =>
          StreamPredictActor.logger.error(exception.getMessage, exception)
          actor ! "Error Input"
      }
    }
    case _ => {
      actor ! "error message"
    }
  }

}
