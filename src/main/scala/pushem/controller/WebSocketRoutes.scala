package pushem.controller

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ws.UpgradeToWebSocket
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import pushem.actor.ClientActor

object WebSocketRoutes extends LazyLogging {

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
