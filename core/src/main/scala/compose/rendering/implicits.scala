package compose.rendering

import java.io.ByteArrayInputStream
import scala.util.{ Failure, Success, Try }

import compose.http.Response.Renderer
import compose.http.Headers

object implicits {

  def encodedStringRenderer(encoding: String): Renderer[String] =
    Renderer.instance[String](bodyStr => {
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
    })

  implicit val stringRenderer: Renderer[String] = encodedStringRenderer("UTF-8")
}
