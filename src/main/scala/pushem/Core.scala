package pushem

import akka.http.scaladsl.Http
import pushem.context.Setup._
import pushem.controller.{Routes, WebSocketRoute}

object Core {

  def main(args: Array[String]) = {
    Http().bindAndHandle(Routes.routes, "localhost", 8080)
    ()
  }
}