package pushem.actor

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Graph, OverflowStrategy, SinkShape, SourceShape}
import com.typesafe.scalalogging.LazyLogging
import pushem.context.Context
import pushem.models.{Publish, PublishSubscribe, PublishSubscribeProtocol, Subscribe}
import pushem.util.EnrichedJson

case class ActorSinkSource(actor: ActorRef, source: Source[Nothing, ActorRef], sink: Graph[SinkShape[Message], Any])

class ClientActor(source: Graph[SourceShape[Message], Any]) extends Actor with LazyLogging {
  override def receive = {
    case p @ Publish(channel, event, data) => logger.info(p.toString)
    case s @ Subscribe(channel) => logger.info(s.toString)
    case _: Unit => logger.debug("Closing client actor") ; context.stop(self)
  }
}

object ClientActor extends LazyLogging with PublishSubscribeProtocol with EnrichedJson {
  private case object Start

  val messageFlow = {
    val messageToPublishSubscribe: PartialFunction[Message, PublishSubscribe] = Function.unlift {
      case TextMessage.Strict(text) => text.asJsonOpt[PublishSubscribe]
      case _ => None
    }
    Flow[Message].collect(messageToPublishSubscribe)
  }

  def create(): ActorSinkSource = {
    val source: Source[Nothing, ActorRef] = Source.actorRef(256, OverflowStrategy.fail)
    val props = Props(new ClientActor(source))
    val client = Context.system.actorOf(props)
    val sink: Graph[SinkShape[Message], Any] = messageFlow.to(Sink.actorRef(client, Start))
    ActorSinkSource(client, source, sink)
  }
}
