package pushem.util

import spray.json.{JsValue, JsonReader, _}

import scala.util.Try

trait EnrichedJson {
  implicit class RichJson(jsValue: JsValue) {
    def asOpt[T](implicit reader: JsonReader[T]): Option[T] = Try(jsValue.convertTo[T]).toOption
    def canConvert[T](implicit reader: JsonReader[T]): Boolean = Try(jsValue.convertTo[T]).isSuccess
  }

  implicit class JsonString(string: String) {
    def asJsonOpt[T](implicit reader: JsonReader[T]): Option[T] = Try(string.parseJson.convertTo[T]).toOption
    def canConvert[T](implicit reader: JsonReader[T]): Boolean = Try(string.parseJson.convertTo[T]).isSuccess
  }
}

