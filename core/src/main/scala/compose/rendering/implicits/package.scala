package compose.rendering

import compose.http.Response.Renderer

/** Convenience package with a collection of implicit renderers
  *
  * {{{
  * // to get them all:
  * import compose.rendering.implicits._
  * }}}
  */
package object implicits {
  implicit val stringRenderer: Renderer[String] = new EncodedStringRenderer("UTF-8")
}
