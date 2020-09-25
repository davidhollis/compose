package compose.rendering

import play.twirl.api.Content
import compose.http.Headers

class TwirlContentRenderer(implicit stringRenderer: Renderer[String])
    extends WrappedRenderer[Content, String](stringRenderer) {

  def transformBody(twirlContent: Content): String = twirlContent.body

  def transformHeaders(twirlContent: Content, initialHeaders: Headers): Headers =
    initialHeaders.replace("Content-Type" -> twirlContent.contentType)

}
