package compose.rendering

import java.io.ByteArrayInputStream
import play.api.libs.json.Writes

import compose.http.Response.Renderer
import compose.http.Headers

/** Renders any object that's convertible into json into and HTTP response body
  *
  * @tparam T
  *   the type of object to render
  */
class JsonRenderer[T: Writes] extends Renderer[T] {

  def render(body: T): Renderer.Result = {
    val jsValue = Writes.of[T].writes(body)
    val jsonBytes = jsValue.toString().getBytes("UTF-8")
    Renderer.Success(
      Headers(
        "Content-Type" -> """application/json; charset="UTF-8"""",
        "Content-Length" -> jsonBytes.length.toString(),
      ),
      new ByteArrayInputStream(jsonBytes),
    )
  }

}
