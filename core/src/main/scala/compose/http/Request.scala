package compose.http

import java.io.InputStream
import scala.io.Source
import scala.util.matching.Regex

import compose.http.attributes.{ Attr, AttrList, NoAttrs }

case class Request[+Body, +Attrs <: AttrList](
  version: Version,
  method: Method,
  target: RequestTarget,
  headers: Headers,
  body: Body,
  extendedAttributes: Attrs,
) {

  def withAttr[A](newAttr: Attr[A, NoAttrs.type]): Request[Body, Attr[A, Attrs]] =
    this.copy[Body, Attr[A, Attrs]](
      extendedAttributes = newAttr.copy[A, Attrs](rest = extendedAttributes)
    )

}

object Request {

  def parse(inputStream: InputStream): Option[Request[InputStream, NoAttrs]] = {
    val headSection = RequestHeadReader.readHeading(inputStream)
    val headerSource = Source.fromString(headSection)
    headerSource.getLines().next match {
      case startLine(Method(method), targetString, Version(version)) => {
        val headers = Headers.parse(headerSource)
        Some(
          Request(
            version,
            method,
            RequestTarget.parse(targetString),
            headers,
            body = inputStream,
            extendedAttributes = NoAttrs,
          )
        )
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

    @SuppressWarnings(
      Array(
        // We're iterating over an unbuffered input stream of unknown size until a specific state is achieved.
        // We need a while loop to read from the input stream and check the condition and a var to hold the current state.
        "scalafix:DisableSyntax.var",
        "scalafix:DisableSyntax.while",
      )
    )
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
