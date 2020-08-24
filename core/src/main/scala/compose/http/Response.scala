package compose.http

import java.io.{ InputStream, OutputStream, OutputStreamWriter }
import scala.annotation.implicitNotFound

case class Response(
  version: Version,
  status: Status,
  headers: Headers,
  body: InputStream,
) {
  lazy val statusLine: String = s"${version} ${status}\r\n"

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
  def apply[B: Renderer](
    rawBody: B,
    version: Version = Version.HTTP_1_1,
    status: Status = Status.OK,
    headers: Headers = Headers.empty,
  ): Response = Renderer[B].render(rawBody) match {
    case Renderer.Success(defaultHeaders, bodyStream) => Response(version, status, defaultHeaders.replace(headers), bodyStream)
    case Renderer.Failure(errorMessage) =>
      Response[String](
        errorMessage,
        version = version,
        status = Status.InternalServerError,
        headers = headers
      )(compose.rendering.implicits.stringRenderer)
  }

  @implicitNotFound("No renderer found for type ${B}")
  trait Renderer[-B] {
    def render(body: B): Renderer.Result
  }

  object Renderer {
    def instance[B](rf: B => Renderer.Result): Renderer[B] = new Renderer[B] { def render(body: B): Renderer.Result = rf(body) }

    def apply[B](implicit r: Renderer[B]): Renderer[B] = r

    sealed trait Result
    case class Success(defaultHeaders: Headers, bodyStream: InputStream) extends Result
    case class Failure(errorMessage: String) extends Result
  }
}
