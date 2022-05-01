import models.WsMsg
import org.scalatestplus.play.PlaySpec
import utils.FileUtil

import java.io.File
import java.util.UUID

class UtilSpec extends PlaySpec {

  "FileUtil" should {
    "Generate upload path success" in {
      val str = FileUtil.getUploadPath(UUID.randomUUID().toString)
      new File(str).exists() mustBe true
    }

    "Generate file path success" in {
      val str = FileUtil.generateFilePath(UUID.randomUUID().toString)
      new File(str).exists() mustBe true
    }

  }

  "Websocket message parse" should {
    "Read success" in {
      val json =
        """{"data": [{
      "id": 9,
      "airline": "GO_FIRST",
      "flight": "G8-336",
      "sourceCity": "Delhi",
      "departureTime": "Afternoon",
      "stops": "zero",
      "arrivalTime": "Evening",
      "destinationCity": "Mumbai",
      "classType": "Economy",
      "duration": 2.25,
      "daysLeft": 1
    },{
      "id": 9,
      "airline": "GO_FIRST",
      "flight": "G8-336",
      "sourceCity": "Delhi",
      "departureTime": "Afternoon",
      "stops": "zero",
      "arrivalTime": "Evening",
      "destinationCity": "Mumbai",
      "classType": "Economy",
      "duration": 2.25,
      "daysLeft": 1
    }]}"""
      WsMsg.apply(json).getFlights.size mustBe 2
    }


    "Seq flight size" in {
      "Read success" in {
        val json =
          """{"data": [{
      "id": 9,
      "airline": "GO_FIRST",
      "flight": "G8-336",
      "sourceCity": "Delhi",
      "departureTime": "Afternoon",
      "stops": "zero",
      "arrivalTime": "Evening",
      "destinationCity": "Mumbai",
      "classType": "Economy",
      "duration": 2.25,
      "daysLeft": 1
    },{
      "id": 9,
      "airline": "GO_FIRST",
      "flight": "G8-336",
      "sourceCity": "Delhi",
      "departureTime": "Afternoon",
      "stops": "zero",
      "arrivalTime": "Evening",
      "destinationCity": "Mumbai",
      "classType": "Economy",
      "duration": 2.25,
      "daysLeft": 1
    }]}"""
        val seq = WsMsg.apply(json).getFlights
        WsMsg.apply(seq).getFlights.size mustBe 2
      }
    }
  }
}
