package compose.http

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

  lazy val all: Set[Method] = Set(
    Get,
    Head,
    Post,
    Put,
    Delete,
    Connect,
    Options,
    Trace,
    Patch,
  )

  lazy val byName: Map[String, Method] =
    all.map { method => (method.toString -> method) }.toMap

  def unapply(methodStr: String): Option[Method] =
    byName.get(methodStr.toUpperCase)

}
