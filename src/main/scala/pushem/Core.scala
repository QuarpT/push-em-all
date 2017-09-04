package pushem

import akka.http.scaladsl.Http
import pushem.context.Context._
import pushem.controller.Routes

object Core {

  def main(args: Array[String]) = {
    Http().bindAndHandle(Routes.routes, "localhost", 8080)
    ()
  }
}
