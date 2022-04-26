package models

import com.fasterxml.jackson.databind.JsonNode
import com.neu.edu.FlightPricePrediction.pojo.Flight
import play.libs.Json
import java.util.Iterator
/**
* @author Caspar
* @date 2022/4/24 22:49 
*/
class WsMsg(flights: Seq[Flight]) {
  def getFlights = flights
}

object WsMsg {
  def apply(flight: Seq[Flight]) = new WsMsg(flight)
  def apply(msg: String): WsMsg = {
    def inner(fs: Seq[Flight], iterator: Iterator[JsonNode]): Seq[Flight] = {
      iterator.hasNext match {
        case true => {
          val flight = Json.fromJson[Flight](iterator.next(), classOf[Flight])
          inner(fs :+ flight, iterator)
        }
        case false => fs
      }
    }
    apply(inner(Nil, Json.parse(msg).get("data").elements()))
  }
}
