package compose.http

// format: off
/** Support for type-safe extended request attributes.
  *
  * =Examples=
  *
  * ==Creating an attribute tag==
  * {{{
  * import compose.html.attributes._
  * import javax.security.auth.x500.X500Principal
  *
  * object ClientCertPrincipal extends AttrTag[X500Principal]("client cert principal")
  * }}}
  *
  * ==Adding a tagged attribute to a request==
  * {{{
  * import compose.html._
  * import compose.rendering.implicits._
  * import scala.util.{ Try, Either, Left, Right }
  *
  * def requireX500Header[B, A](
  *   req: Request[B, A]
  * ): Either[Response, Request[B, Attr[X500Principal, A]]] =
  *   req.headers
  *     .get("X-Client-Cert-Principal")
  *     .flatMap { principal => Try(new X500Principal(principal)).toOption }
  *     .fold {
  *       // If we don't find or can't parse the header, send back an error response
  *       Left(Response(
  *         "Missing or invalid client cert principal",
  *         status = Status.BadRequest,
  *       ))
  *     } { (principal: X500Principal) =>
  *       // If we find a valid header, attach it to the request and keep going
  *       Right(req.withAttr(ClientCertPrincipal ~> principal))
  *     }
  * }}}
  *
  * There are two important things to note in the above:
  *
  *   1. In the type '''`Request[B, Attr[X500Principal, A]]`''', the inner type '''`Attr[X500Principal, A]`'''
  *      can be read as "An attribute set with an `X500Principal` and the rest of the attributes in
  *      the set `A`".
  *   1. The expression '''`req.withAttr(ClientCertPrincipal ~> principal)`''' is what's actually adding
  *      the attribute.
  *
  * ==Getting an attribute out directly with its tag==
  * {{{
  * req.extendedAttributes(ClientCertPrincipal) // : Option[X500Principal]
  * }}}
  *
  * ==Getting an attribute out by pattern matching==
  * {{{
  * req match {
  *   case Request(_, _, _, _, _, ClientCertPrincipal(principal)) =>
  *     doSomethingWith(principal)
  *   // ...
  * }
  * }}}
  *
  * ==Using implicits to require a certain type of attribute==
  * {{{
  * def checkAuthorized[B, A](
  *   req: Request[B, A]
  * )(
  *   implicit
  *   hasCert: A HasAttr X500Principal // syntactic sugar for HasAttr[A, X500Principal]
  * ): Boolean = ???
  * }}}
  */
package object attributes {
  // format: on

  implicit class AttrMapBuilder[T1](kv1: (AttrKey[T1], T1)) {

    def :@:[T2](kv2: (AttrKey[T2], T2)): AttrMap =
      (new AttrMap) + kv1 + kv2

  }

}
