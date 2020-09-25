package compose.rendering

import play.twirl.api.Content

import compose.rendering.Renderer

/** Convenience package with a collection of implicit renderers
  *
  * {{{
  * // to get them all:
  * import compose.rendering.implicits._
  * }}}
  */
package object implicits {
  implicit val stringRenderer: Renderer[String] = new EncodedStringRenderer("UTF-8")
  implicit val twirlContentRenderer: Renderer[Content] = new EncodedTwirlContentRenderer("UTF-8")
}
