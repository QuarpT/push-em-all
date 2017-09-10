package pushem.controller
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import pushem.context.Context._

object Routes {
  val routes: Route = WebSocketRoutes.indexRoute ~ WebSocketRoutes.webSocketRoute
}
