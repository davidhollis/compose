package compose.http

import java.io.{ ByteArrayInputStream, InputStream, OutputStream, OutputStreamWriter }
import scala.io.Source

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
  def withStringBody(
    version: Version,
    status: Status,
    headers: Headers,
    bodyStr: String,
    encoding: String = "UTF-8",
  ): Response = Response(version, status, headers, new ByteArrayInputStream(bodyStr.getBytes(encoding)))
}