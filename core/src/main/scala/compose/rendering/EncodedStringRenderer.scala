package compose.rendering

import java.io.ByteArrayInputStream
import scala.util.{ Failure, Success, Try }

import compose.http.Response.Renderer
import compose.http.Headers

/** Renders a string into an HTTP response body, using a specific encoding.
  *
  * @param encoding
  *   the encoding to use
  */
class EncodedStringRenderer(encoding: String) extends Renderer[String] {

  def render(bodyStr: String): Renderer.Result = {
    Try(bodyStr.getBytes(encoding)) match {
      case Success(bodyBytes) =>
        Renderer.Success(
          Headers(
            "Content-Type" -> s"""text/plain; charset="${encoding}"""",
            "Content-Length" -> bodyBytes.length.toString(),
          ),
          new ByteArrayInputStream(bodyBytes),
        )
      case Failure(_) => Renderer.Failure(s"Unsupported encoding: $encoding")
    }
  }

}
