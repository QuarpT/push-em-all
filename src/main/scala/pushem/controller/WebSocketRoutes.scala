package pushem.controller

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import pushem.actor.ClientActor
import pushem.context.Context._

object WebSocketRoutes extends LazyLogging {

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

  val webSocketRoute: Route = path("ws") {
    extractRequest { request =>
      complete(
        request.header[UpgradeToWebSocket] match {
          case Some(upgrade) =>
            val sinkSource = ClientActor.create()
            upgrade.handleMessagesWithSinkSource(sinkSource.sink, sinkSource.source)
          case None => HttpResponse(400, entity = "Not a valid websocket request!")
        }
      )
    }
  }

}
