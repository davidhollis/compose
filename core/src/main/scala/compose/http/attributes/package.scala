package compose.http

/** Support for type-safe extended request attributes.
  *
  * = Examples: =
  *
  * == Creating an attribute key ==
  * {{{
  * import compose.html.attributes._
  * import javax.security.auth.x500.X500Principal
  *
  * object ClientCertPrincipal extends AttrKey[X500Principal]("client cert principal")
  * }}}
  *
  * == Adding a keyed attribute to a request ==
  * {{{
  * import compose.html._
  * import compose.rendering.implicits._
  * import scala.util.{ Either, Left, Right }
  *
  * def requireX500Header[B](req: Request[B]): Either[Response, Request[B]] =
  *   extractX500Principal(req.headers) match {
  *     case Some(principal) => Right(req.withAttr(ClientCertPrincipal -> principal))
  *     case None            => Left(errorResponse())
  *   }
  * }
  * }}}
  *
  * The expression '''`req.withAttr(ClientCertPrincipal -> principal)`''' is what's actually adding
  * the attribute.
  *
  * == Getting an attribute directly ==
  * {{{
  * req.extendedAttributes.get(ClientCertPrincipal) // : Option[X500Principal]
  * }}}
  *
  * == Getting a single attribute by pattern matching ==
  * {{{
  * req match {
  *   case Request(_, _, _, _, _, ClientCertPrincipal(principal)) =>
  *     doSomethingWith(principal)
  *   // ...
  * }
  * }}}
  *
  * == Getting multiple attributes at the same time by pattern matching ==
  * {{{
  * req match {
  *   case Request(_, _, _, _, _, ClientCertPrincipal(principal) :@: SomeOtherKey(anotherValue)) =>
  *     doSomethingElseWith(principal, anotherValue)
  *   // ...
  * }
  * }}}
  */
package object attributes {

  /** Extension class for combining two typed key-value pairs into an [[AttrMap]].
    *
    * This operator is right-associative: if both pairs have the same key, the resulting map will
    * contain the value from the lefthand pair.
    *
    * @constructor
    * @tparam T1 The type of the righthand value
    * @param kv1 The righthand key-value pair
    */
  implicit class AttrMapBuilder[T1](kv1: (AttrKey[T1], T1)) {

    /** Combine the righthand key-value pair with the given lefthand pair.
      *
      * @tparam T2 The type of the lefthand value
      * @param kv2 The lefthand key-value pair
      * @return The combined map
      */
    def :@:[T2](kv2: (AttrKey[T2], T2)): AttrMap =
      (new AttrMap) + kv1 + kv2

  }

}
