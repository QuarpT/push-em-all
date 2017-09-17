package ClientActorTest.pushem

import akka.actor.{ActorSystem, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.http.scaladsl.model.ws.TextMessage
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import org.scalatest._
import pushem.actor.ClientActor
import pushem.models.{ChannelMessage, Publish, Subscribe, UnSubscribe}
import pushem.util.Hashing
import spray.json.{JsObject, JsString}
import scala.concurrent.duration._
import scala.language.postfixOps

class ClientActorSpec extends TestKit(ActorSystem("ClientActor")) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll
  with Hashing {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A ClientActor" should "send channel messages to client actor" in {
    val sourceActorProbe = TestProbe()
    val channelMessage = ChannelMessage("channel", "event", JsObject("field" -> JsString("value")))
    val clientActor = system.actorOf(Props[ClientActor])
    clientActor ! sourceActorProbe.ref
    clientActor ! channelMessage
    sourceActorProbe.expectMsg(TextMessage.Strict("""{"channel":"channel","event":"event","data":{"field":"value"},"type":"channelMessage"}"""))
  }

  it should "publish messages to DistributedPubSubMediator" in {
    val mediator = DistributedPubSub(system).mediator

    val pubSubProbe = TestProbe()
    mediator ! DistributedPubSubMediator.Subscribe(sha256("channel"), pubSubProbe.ref)

    val publishMessage = Publish("channel", "event", JsObject("field" -> JsString("value")))
    val channelMessage = ChannelMessage("channel", "event", JsObject("field" -> JsString("value")))

    val clientActor = system.actorOf(Props[ClientActor])
    clientActor ! publishMessage
    pubSubProbe.expectMsg(channelMessage)
  }

  it should "send published messages to client after subscribing to them via the mediator" in {
    val mediator = DistributedPubSub(system).mediator

    val sourceActorProbe = TestProbe()
    val subscribeMessage = Subscribe("channel")
    val channelMessage = ChannelMessage("channel", "event", JsObject("field" -> JsString("value")))

    val clientActor = TestActorRef[ClientActor]
    clientActor ! sourceActorProbe.ref
    clientActor ! subscribeMessage

    mediator ! DistributedPubSubMediator.Publish(sha256("channel"), channelMessage)
    sourceActorProbe.expectMsg(TextMessage.Strict("""{"channel":"channel","event":"event","data":{"field":"value"},"type":"channelMessage"}"""))
  }

  it should "not receive messages after unsubscribing" in {
    val mediator = DistributedPubSub(system).mediator

    val sourceActorProbe = TestProbe()
    val subscribeMessage = Subscribe("channel")
    val unsubscribeMessage = UnSubscribe("channel")
    val channelMessage = ChannelMessage("channel", "event", JsObject("field" -> JsString("value")))

    val clientActor = TestActorRef[ClientActor]
    clientActor ! sourceActorProbe.ref
    clientActor ! subscribeMessage
    clientActor ! unsubscribeMessage

    mediator ! DistributedPubSubMediator.Publish(sha256("channel"), channelMessage)
    sourceActorProbe.expectNoMsg(100 milli)
  }

  it should "close source after receiving unit" in {
    val clientActor = TestActorRef[ClientActor]
    val sourceActorProbe = TestProbe()
    val probe = TestProbe()
    probe.watch(clientActor)
    probe.watch(sourceActorProbe.ref)

    clientActor ! sourceActorProbe.ref
    clientActor ! ((): Unit)

    probe.expectTerminated(sourceActorProbe.ref)
    probe.expectTerminated(clientActor)
  }

}
