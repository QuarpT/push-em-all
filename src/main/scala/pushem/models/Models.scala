package pushem.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, _}

import scala.util.Try

trait PublishSubscribeProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val subscribeFormat: JsonFormat[Subscribe] = jsonFormat1(Subscribe.apply)
  implicit val publishFormat: JsonFormat[Publish] = jsonFormat3(Publish.apply)
  implicit val pubSubFormat: JsonReader[PublishSubscribe] = json => Try(json.convertTo[Publish]).getOrElse(json.convertTo[Subscribe])
}

sealed trait PublishSubscribe

case class Subscribe(channel: String) extends PublishSubscribe

case class Publish(channel: String, event: String, data: JsValue) extends PublishSubscribe
