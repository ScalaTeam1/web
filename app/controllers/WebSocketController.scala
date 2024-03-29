package controllers

import actors.StreamPredictActor
import akka.actor.{ActorSystem, Props}
import com.google.inject.Inject
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import services.PredictorService

/**
* @author Caspar
* @date 2022/4/25 21:09 
*/class WebSocketController @Inject()(predictorService: PredictorService,
                                      cc: ControllerComponents)(implicit system: ActorSystem)
  extends AbstractController(cc) {
  private val logger = Logger(this.getClass)

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef[String, String] { out  =>
      Props(new StreamPredictActor(predictorService)(out))
    }
  }

}
