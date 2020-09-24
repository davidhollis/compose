package compose

/** Methods for rendering various types of objects into HTTP responses
  *
  * == Rendering ==
  *
  * Rendering in compose is the process of converting an object into a byte stream (and,
  * optionally, a set of default headers). Generaly, renderers won't be invoked directly, but
  * instead provided either implicitly or explicitly to a [[compose.http.Response]] factory.
  *
  * === Implicit Rendering ===
  *
  * The most common case is rendering implicitly. Most default renderers can be brought into scope
  * with:
  *
  * {{{
  * import compose.rendering.implicits._
  * }}}
  *
  * However, rendering to JSON is a special case. Because so many types of objects are renderable
  * as JSON, to minimize the number of times you'll need to explicitly specify a renderer, the
  * implicit JSON renderer is located in its own package:
  *
  * {{{
  * import compose.rendering.implicits.json._
  * }}}
  *
  * Generally, you'll only want one of those two imports in any given scope.
  *
  * === Custom Rendering ===
  *
  * If you need to provide a custom renderer for a particular type, you can either subclass
  * [[compose.http.Response.Renderer]] or compose a function with an existing renderer:
  *
  * {{{
  * import compose.http.Response.Renderer
  * import compose.rendering.EncodedStringRenderer
  *
  * case class User(name: String, email: String) {
  *   def obfuscatedEmail: String = ???
  * }
  *
  * object User {
  *   // EncodedStringRenderer is a Renderer[String], so we can compose it with a function
  *   // of type User => String to get a Renderer[User]
  *   implicit val renderer: Renderer[User] = new EncodedStringRenderer("UTF-8").compose { u =>
  *     s"""User: "${u.name}" <${u.obfuscatedEmail}>"""
  *   }
  * }
  * }}}
  *
  * === Explicit Rendering ===
  *
  * Occasionally, you may need to explicitly specify a renderer. This is most likely because there
  * exists an implicit renderer for the type in question, but you need to handle the object in a
  * different way than it does.
  *
  * For example, the default renderer for strings encodes them as UTF-8. If you need a different
  * encoding, you can specify that explicitly:
  *
  * {{{
  * import compose.http.Response
  * import compose.rendering.EncodedStringRenderer
  * import compose.rendering.implicits._
  *
  * val respUTF8 = Response("Hello!")
  * // val respUTF8: compose.http.Response = Response(HTTP/1.1,200 OK,Headers(HashMap(content-length -> ArraySeq(6), content-type -> ArraySeq(text/plain; charset="UTF-8"))),java.io.ByteArrayInputStream@6cb69ce0)
  * respUTF8.body.readAllBytes()
  * // val res1: Array[Byte] = Array(72, 101, 108, 108, 111, 33)
  *
  * val respUTF16 = Response("Hello!")(new EncodedStringRenderer("UTF-16"))
  * // val respUTF16: compose.http.Response = Response(HTTP/1.1,200 OK,Headers(HashMap(content-length -> ArraySeq(14), content-type -> ArraySeq(text/plain; charset="UTF-16"))),java.io.ByteArrayInputStream@6eff87ca)
  * respUTF16.body.readAllBytes()
  * // val res3: Array[Byte] = Array(-2, -1, 0, 72, 0, 101, 0, 108, 0, 108, 0, 111, 0, 33)
  * }}}
  *
  * @see [[compose.http.Response.Renderer]]
  */
package object rendering {}
