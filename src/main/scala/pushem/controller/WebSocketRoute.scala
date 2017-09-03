package pushem.controller

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import pushem.context.Setup._

object WebSocketRoute extends LazyLogging {

  val greeterWebSocketService = Flow[Message].mapConcat {
    case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      Nil
  }

  val indexRoute: Route = path("index") {
    get {
      complete("Hello")
    }
  }

  val webSocketRoute: Route = path("greeter") {
    handleWebSocketMessages(greeterWebSocketService)
  }

}
