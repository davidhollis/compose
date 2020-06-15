package compose.http

import scala.io.Source

case class Request(
  version: Float,
  method: Request.Method,
  path: String,
  headers: Headers,
  body: Source,
)

object Request {
  def parse(source: Source): Option[Request] = None // TODO

  sealed abstract class Method(override val toString: String)
  object Method {
    case object Get extends Method("GET")
    case object Head extends Method("HEAD")
    case object Post extends Method("POST")
    case object Put extends Method("PUT")
    case object Delete extends Method("DELETE")
    case object Connect extends Method("CONNECT")
    case object Options extends Method("OPTIONS")
    case object Trace extends Method("TRACE")
    case object Patch extends Method("PATCH")
  }
}