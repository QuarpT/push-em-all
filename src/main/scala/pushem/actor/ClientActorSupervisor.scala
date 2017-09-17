package pushem.actor

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, OneForOneStrategy, Props}
import akka.pattern.ask
import com.typesafe.scalalogging.LazyLogging
import pushem.context.Context
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.NonFatal

class ClientActorSupervisor extends Actor with LazyLogging {
  import ClientActorSupervisor._

  override val supervisorStrategy = OneForOneStrategy() {
    case NonFatal(e) => logger.error("Error in client actor", e) ; Stop
  }

  override def receive = {
    case CreateClient => sender() ! ClientActor.createActorSourceSink(context.actorOf(Props[ClientActor]))
  }
}

object ClientActorSupervisor {
  private case object CreateClient
  val supervisor = Context.system.actorOf(Props[ClientActorSupervisor])

  def createClient(): Future[ActorSinkSource] = {
    ask(supervisor, CreateClient)(5000 milli).mapTo[ActorSinkSource]
  }
}
