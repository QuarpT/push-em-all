package pushem.controller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object Routes {
  val routes: Route = WebSocketRoute.indexRoute ~ WebSocketRoute.webSocketRoute
}
