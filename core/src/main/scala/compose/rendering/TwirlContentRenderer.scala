package compose.rendering

import play.twirl.api.Content
import compose.http.Headers

/** A renderer for any type of content produced from a Twirl template.
  *
  * Because twirl content resolves into a string, we delegate encoding to a string renderer.
  * This defaults to the implicit (UTF-8) string renderer, but that can be overridden by passing
  * a different renderer. Note that this will overwrite the `Content-Type` header with the media
  * type of the Twirl content buffer.
  *
  * @param stringRenderer
  *   The renderer to delegate to once the content has been converted to a string
  *
  * @see Twirl: https://github.com/playframework/twirl
  */
class TwirlContentRenderer(implicit stringRenderer: Renderer[String])
    extends WrappedRenderer[Content, String](stringRenderer) {

  /** Convert the twirl content to a string to be passed to the base renderer.
    *
    * @param twirlContent The twirl content
    * @return A string which will be rendered into a response stream
    */
  def transformBody(twirlContent: Content): String = twirlContent.body

  /** Replace the `Content-Type` header with the appropriate one based on the media type of the
    * Twirl content.
    *
    * In addition, if we recognize that the base renderer is an [[EncodedStringRenderer]], we
    * include the encoding the content type.
    *
    * @param twirlContent The Twirl content
    * @param initialHeaders The set of headers produced by the base renderer
    * @return The new set of headers this renderer will return to the caller
    */
  override def transformHeaders(twirlContent: Content, initialHeaders: Headers): Headers =
    stringRenderer match {
      case encoded: EncodedStringRenderer =>
        initialHeaders.replace(
          "Content-Type",
          s"""${twirlContent.contentType}; charset="${encoded.encoding}"""",
        )
      case _ => initialHeaders.replace("Content-Type", twirlContent.contentType)
    }

}
