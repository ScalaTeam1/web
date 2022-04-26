package actors

import akka.actor.{Actor, ActorRef}
import com.google.inject.Inject
import models.WsMsg
import services.PredictorService
/**
* @author Caspar
* @date 2022/4/24 19:54 
*/
object StreamPredictActor

class StreamPredictActor @Inject()(service: PredictorService)(actor: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: String => {
      val r = service.predict(WsMsg(msg).getFlights)
      actor ! service.dfToJson(r)
    }
  }

}
