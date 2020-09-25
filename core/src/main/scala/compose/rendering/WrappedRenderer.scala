package compose.rendering

import compose.http.Headers

abstract class WrappedRenderer[-T, Base](private val baseRenderer: Renderer[Base])
    extends Renderer[T] {

  def transformBody(body: T): Base

  def transformHeaders(body: T, initialHeaders: Headers): Headers

  def render(body: T): Renderer.Result = {
    baseRenderer.render(transformBody(body)) match {
      case Renderer.Success(defaultHeaders, bodyStream) =>
        Renderer.Success(transformHeaders(body, defaultHeaders), bodyStream)
      case failure: Renderer.Failure => failure
    }
  }

}
