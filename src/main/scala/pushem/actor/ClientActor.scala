package pushem.actor

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Graph, OverflowStrategy, SinkShape}
import com.typesafe.scalalogging.LazyLogging
import pushem.context.Context
import pushem.models._
import pushem.util.{EnrichedJson, Hashing}
import spray.json._

case class ActorSinkSource(source: Source[Nothing, ActorRef], sink: Graph[SinkShape[Message], Any])

class ClientActor extends Actor with LazyLogging with Hashing {
  val mediator = DistributedPubSub(context.system).mediator
  var wsSend: Option[ActorRef] = None

  override def receive = {

    case ref: ActorRef => wsSend = Some(ref)

    case p @ Publish(channel, _, _, _) =>
      logger.debug(s"Publish $p")
      mediator ! DistributedPubSubMediator.Publish(sha256(channel), ChannelMessage.from(p))

    case m @ ChannelMessage(_, _, _, _) =>
      logger.debug(m.toString)
      wsSend.foreach(_ ! TextMessage(m.toJson.compactPrint))

    case s @ Subscribe(channel, _) =>
      logger.debug(s"Subscribe $s")
      throw new Exception("ahh")
      mediator ! DistributedPubSubMediator.Subscribe(sha256(channel), self)

    case s @ UnSubscribe(channel, _) =>
      logger.debug(s"UnSubscribe $s")
      mediator ! DistributedPubSubMediator.Unsubscribe(sha256(channel), self)

    case _: Unit =>
      logger.debug("Closing client actor")
      context.stop(self)

  }

  override def postStop: Unit = {
    wsSend.foreach(context.stop)
  }
}

object ClientActor extends LazyLogging with PublishSubscribeProtocol with EnrichedJson {
  private case object Start

  val messageFlow = {
    val messageToPublishSubscribe: PartialFunction[Message, PubSub] = Function.unlift {
      case TextMessage.Strict(text) => text.asJsonOpt[PubSub]
      case _ => None
    }
    Flow[Message].collect(messageToPublishSubscribe)
  }

  def create(): ActorSinkSource = {
    val clientActor = Context.system.actorOf(Props[ClientActor])

    val source: Source[Nothing, ActorRef] = Source.actorRef(256, OverflowStrategy.fail).mapMaterializedValue { actorRef =>
      // A little funky but I don't see another way of getting the source actorRef to the client actor
      clientActor ! actorRef
      actorRef
    }

    val sink: Graph[SinkShape[Message], Any] = messageFlow.to(Sink.actorRef(clientActor, Start))

    ActorSinkSource(source, sink)
  }
}
