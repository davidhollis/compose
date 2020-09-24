package compose.rendering.implicits

import play.api.libs.json.Writes

import compose.rendering.JsonRenderer

/** Package containing the implicit json renderer.
  *
  * This is split into its own package to avoid conflicts in implcit resolution involving classes
  * that have both a `Writes` and a `Renderer`.
  *
  * {{{
  * // to make the json renderer available:
  * import compose.rendering.implicits.json._
  * }}}
  */
package object json {

  /** Construct a json renderer for any type that's convertible to json.
    *
    * @tparam T the type to render as json
    * @return a renderer for `T`
    */
  implicit def jsonRenderer[T: Writes] = new JsonRenderer[T]
}
