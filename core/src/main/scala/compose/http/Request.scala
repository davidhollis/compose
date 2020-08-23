package compose.http

import java.io.{ InputStream, InputStreamReader, BufferedReader }
import scala.io.Source
import scala.util.Try
import scala.util.matching.Regex

case class Request[+B](
  version: Version,
  method: Method,
  target: RequestTarget,
  headers: Headers,
  body: B,
)

object Request {
  def parse(inputStream: InputStream): Option[Request[InputStream]] = {
    val headSection = RequestHeadReader.readHeading(inputStream)
    val headerSource = Source.fromString(headSection)
    headerSource.getLines().next match {
      case startLine(Method(method), targetString, Version(version)) => {
        val headers = Headers.parse(headerSource)
        Some(Request(
          version,
          method,
          RequestTarget.parse(targetString),
          headers,
          body=inputStream,
        ))
      }
      case _ => None // Invalid start line
    }
  }

  private lazy val startLine: Regex = {
    val allMethodsAlternation = Method.all.map(_.toString).mkString("|")
    s"""^(${allMethodsAlternation}) (.*) (HTTP/[0-9.]+)$$""".r
  }

  private object RequestHeadReader {
    private val cr = '\r'.toByte
    private val lf = '\n'.toByte

    private class ReaderState(val terminal: Boolean)(val next: Byte => ReaderState)

    private val StartState: ReaderState = new ReaderState(false)(b =>
      if (b == cr) CrState
      else StartState
    )
    private val CrState: ReaderState = new ReaderState(false)(b =>
      if (b == cr) CrState
      else if (b == lf) CrLfState
      else StartState
    )
    private val CrLfState: ReaderState = new ReaderState(false)(b =>
      if (b == cr) CrLfCrState
      else StartState
    )
    private val CrLfCrState: ReaderState = new ReaderState(false)(b =>
      if (b == cr) CrState
      else if (b == lf) CrLfCrLfState
      else StartState
    )
    private val CrLfCrLfState: ReaderState = new ReaderState(true)(_ => CrLfCrLfState)

    def readHeading(inputStream: InputStream): String = {
      val heading = new StringBuilder()
      var currentState: ReaderState = StartState
      val nextByte: Array[Byte] = new Array[Byte](1)

      while (!currentState.terminal && (inputStream.read(nextByte) > 0)) {
        heading += nextByte(0).toChar
        currentState = currentState.next(nextByte(0))
      }

      heading.toString()
    }
  }
}