package compose.rendering

import play.twirl.api.Content

class EncodedTwirlContentRenderer(encoding: String) extends Renderer[Content] {
  private val baseRenderer = new EncodedStringRenderer(encoding)

  def render(twirlContent: Content): Renderer.Result = {
    val baseResult = baseRenderer.render(twirlContent.body)
    baseResult match {
      case Renderer.Success(defaultHeaders, bodyStream) =>
        Renderer.Success(
          defaultHeaders.replace("Content-Type" -> twirlContent.contentType),
          bodyStream,
        )
      case failure: Renderer.Failure => failure
    }
  }

}
