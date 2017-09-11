package pushem.controller
import akka.http.scaladsl.server.Route

object Routes {
  val routes: Route = WebSocketRoutes.webSocketRoute
}
