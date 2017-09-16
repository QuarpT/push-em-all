package pushem.controller

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ws.UpgradeToWebSocket
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import pushem.actor.ClientActorSupervisor
import pushem.context.Context.executionContext

object WebSocketRoutes extends LazyLogging {

  val webSocketRoute: Route = path("ws") {
    extractRequest { request =>
      complete(
        request.header[UpgradeToWebSocket] match {
          case Some(upgrade) =>
            ClientActorSupervisor.createClient().map { sinkSource =>
              upgrade.handleMessagesWithSinkSource(sinkSource.sink, sinkSource.source)
            }
          case None => HttpResponse(400, entity = "Not a valid websocket request!")
        }
      )
    }
  }

}
