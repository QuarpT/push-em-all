package pushem.models

import pushem.util.EnrichedJson
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, _}

trait PublishSubscribeProtocol extends EnrichedJson with DefaultJsonProtocol {
  implicit val pubSubFormat: JsonReader[PubSub] = { json =>
    json.asOpt[JsObject].flatMap(_.fields.get("type").flatMap(_.asOpt[String])) match {
      case Some(Subscribe.messageType) => json.convertTo[Subscribe]
      case Some(Publish.messageType) => json.convertTo[Publish]
      case Some(UnSubscribe.messageType) => json.convertTo[UnSubscribe]
      case _ => throw new Exception(s"Unable to parse $json to PubSub")
    }
  }
}

sealed trait PubSub

case class Subscribe(channel: String, `type`: String = Subscribe.messageType) extends PubSub

object Subscribe extends DefaultJsonProtocol  {
  val messageType = "subscribe"
  implicit val subscribeFormat: JsonFormat[Subscribe] = jsonFormat2(Subscribe.apply)
}

case class Publish(channel: String, event: String, data: JsValue, `type`: String = Publish.messageType) extends PubSub

object Publish extends DefaultJsonProtocol {
  val messageType = "publish"
  implicit val publishFormat: JsonFormat[Publish] = jsonFormat4(Publish.apply)
}

case class UnSubscribe(channel: String, `type`: String = UnSubscribe.messageType) extends PubSub

object UnSubscribe extends DefaultJsonProtocol  {
  val messageType = "unsubscribe"
  implicit val subscribeFormat: JsonFormat[UnSubscribe] = jsonFormat2(UnSubscribe.apply)
}

case class ChannelMessage(channel: String, event: String, data: JsValue)

object ChannelMessage extends DefaultJsonProtocol {
  val messageType = "channelMessage"
  implicit val channelMessageFormat: JsonFormat[ChannelMessage] = jsonFormat3(ChannelMessage.apply)
  def from(publish: Publish) = ChannelMessage(publish.channel, publish.event, publish.data)
}
