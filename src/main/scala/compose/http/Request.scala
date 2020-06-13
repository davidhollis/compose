package compose.http

case class Request(
  version: Float,
  method: Request.Method,
  path: String,
  headers: Map[String, String],
  // body: some kind of stream
)

object Request {
  sealed abstract class Method(override val toString: String)
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