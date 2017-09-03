package pushem.context

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Setup {
  implicit val system = ActorSystem("pushem")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
}
