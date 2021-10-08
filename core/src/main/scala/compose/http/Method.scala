package compose.http

/** An HTTP method as defined in RFC 2616. The list of all instances can be found in [[Method$]].
  *
  * @see
  *   RFC 2616: [[https://tools.ietf.org/html/rfc2616]]
  */
sealed abstract class Method(override val toString: String) {
  def unapply(req: Request[_]): Boolean = req.method == this
}

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

  /** The set of all methods. */
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

  /** A map associating method names with their [[Method]] instances. */
  lazy val byName: Map[String, Method] =
    all.map { method => (method.toString -> method) }.toMap

  /** Extract a [[Method]] from a string (method name).
    *
    * @param methodStr
    *   a string, perhaps containing a method name
    * @return
    *   `Some(m: Method)` if `methodStr` was a method name; `None` if not.
    */
  def unapply(methodStr: String): Option[Method] =
    byName.get(methodStr.toUpperCase)

}
