package compose.rendering

import compose.http.Headers

/** A composite renderer that uses another type of renderer.
  *
  * The wrapped renderer first transforms its body into a type of value that the base renderer can
  * handle. The it renders that transformed body using the base renderer, and optionally modifies
  * the initial set of headers returned by the base renderer.
  *
  * @param baseRenderer the renderer we'll delegate most of the work to
  */
abstract class WrappedRenderer[-T, Base](private val baseRenderer: Renderer[Base])
    extends Renderer[T] {

  /** Transform the body into a form the base renderer can accept.
    *
    * @param body The raw body passed to this renderer
    * @return The transformed body suitable for the base renderer
    */
  def transformBody(body: T): Base

  /** Transform the initial set of headers produced by the base renderer.
    *
    * @param body The original body passed to this renderer
    * @param initialHeaders The set of headers provided by the base renderer
    * @return The new set of headers this renderer will return to the caller
    */
  def transformHeaders(body: T, initialHeaders: Headers): Headers = initialHeaders

  def render(body: T): Renderer.Result = {
    baseRenderer.render(transformBody(body)) match {
      case Renderer.Success(defaultHeaders, bodyStream) =>
        Renderer.Success(transformHeaders(body, defaultHeaders), bodyStream)
      case failure: Renderer.Failure => failure
    }
  }

}
