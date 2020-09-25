package compose.http

import java.io.{ InputStream, OutputStream, OutputStreamWriter }

import compose.rendering.Renderer

/** An HTTP response
  *
  * @param version
  *   the version of HTTP this server speaks
  * @param status
  *   the response status
  * @param headers
  *   the response headers
  * @param body
  *   a stream that the server will read the response body from
  */
case class Response(
  version: Version,
  status: Status,
  headers: Headers,
  body: InputStream,
) {
  private lazy val statusLine: String = s"${version} ${status}\r\n"

  /** Write this response out to an output stream.
    *
    * @param out
    *   the output stream to write to
    */
  def writeTo(out: OutputStream): Unit = {
    val writer = new OutputStreamWriter(out, Headers.encoding)
    writer.write(statusLine)
    writer.write(headers.render)
    writer.write("\r\n\r\n")
    writer.flush()
    body.transferTo(out)
  }

}

object Response {

  /** Construct a response using a renderer.
    *
    * @param rawBody
    *   an object that will be rendered into the request body
    * @param version
    *   the HTTP version
    * @param status
    *   the response status
    * @param headers
    *   the response headers
    * @return
    *   a response
    */
  def apply[B: Renderer](
    rawBody: B,
    version: Version = Version.HTTP_1_1,
    status: Status = Status.OK,
    headers: Headers = Headers.empty,
  ): Response =
    Renderer[B].render(rawBody) match {
      case Renderer.Success(defaultHeaders, bodyStream) =>
        Response(version, status, defaultHeaders.replace(headers), bodyStream)
      case Renderer.Failure(errorMessage) =>
        Response[String](
          errorMessage,
          version = version,
          status = Status.InternalServerError,
          headers = headers,
        )(compose.rendering.implicits.stringRenderer)
    }

}
