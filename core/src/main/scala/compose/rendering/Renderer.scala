package compose.rendering

import java.io.InputStream
import scala.annotation.implicitNotFound

import compose.http.Headers

/** Typeclass expressing that `B` can be rendered into an HTTP response body.
  *
  * @tparam B
  *   the body type that can be rendered
  */
@implicitNotFound("No renderer found for type ${B}")
trait Renderer[-B] {

  /** Render the given object into an HTTP response body.
    *
    * @param body
    *   the object to render
    * @return
    *   the results of the render operation
    */
  def render(body: B): Renderer.Result

  /** Create a new renderer for a type `T` by composing this one with a function
    * of type `T => B`.
    *
    * @tparam T the type of the new renderer
    * @param fn a function mapping values of type T into values this renderer can handle
    * @return a new renderer of type `T`
    */
  def compose[T](fn: T => B): Renderer[T] = {
    val outerRenderer = this
    new Renderer[T] {
      def render(body: T): Renderer.Result = outerRenderer.render(fn(body))
    }
  }

}

object Renderer {

  /** Convenience method for constructing a [[Renderer]] from a render function
    *
    * @tparam B
    *   the type to construct a renderer for
    * @param rf
    *   an implementation for the render method of the new renderer
    * @return
    *   the new renderer
    */
  def instance[B](rf: B => Renderer.Result): Renderer[B] =
    new Renderer[B] { def render(body: B): Renderer.Result = rf(body) }

  /** Summoner method for an implicit renderer instance.
    *
    * @tparam B
    *   the type of renderer to summon
    * @return
    *   a renderer for type `B`
    */
  def apply[B](implicit r: Renderer[B]): Renderer[B] = r

  /** The result of a response rendering operation. */
  sealed trait Result

  /** The result of a successful render operation.
    *
    * Note that the default headers may still be overridden by the caller of the render operation.
    *
    * @param defaultHeaders
    *   a set of headers that should be applied to the response
    * @param bodyStream
    *   the rendered body
    */
  case class Success(defaultHeaders: Headers, bodyStream: InputStream) extends Result

  /** The result of a failed render operation.
    *
    * @param errorMessage
    *   a message describing the error
    */
  case class Failure(errorMessage: String) extends Result
}
