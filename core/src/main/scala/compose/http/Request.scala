package compose.http

import java.io.InputStream
import scala.io.Source
import scala.util.matching.Regex

import compose.http.attributes.{ Attr, AttrList, NoAttrs }

/** An HTTP request with typed body and optional extended attributes.
  *
  * Requests intially have a body of type [[java.io.InputStream]] and attribute type of
  * [[compose.http.attributes.NoAttrs]] (that is, they have no extended attributes). Body parsers or
  * other middleware may convert such requests into [[Request]] s of other types.
  *
  * @tparam Body
  *   the type of the request body
  * @tparam Attrs
  *   the types of all extended attributes. Generally, functions which operate on requests don't
  *   fully specify the attribute type, but instead assert the presence of specific relevant
  *   attributes by taking an implicit evidence parameter of type
  *   [[compose.http.attributes.HasAttr]].
  * @param version
  *   the HTTP version the client who wrote this request speaks
  * @param method
  *   this request's method
  * @param target
  *   a description of the resource being requested, usually a path
  * @param headers
  *   the set of headers included with this request
  * @param body
  *   the request body
  * @param extendedAttributes
  *   any additional attributes attached to this request by middleware
  */
case class Request[+Body, +Attrs <: AttrList](
  version: Version,
  method: Method,
  target: RequestTarget,
  headers: Headers,
  body: Body,
  extendedAttributes: Attrs,
) {

  /** Transform the body of this request by applying a function to it.
    *
    * @tparam T
    *   the new body type
    * @param f
    *   the function to apply to the body
    * @return
    *   a request identical to this one except for the body
    */
  def mapBody[T](f: Body => T): Request[T, Attrs] = this.copy(body = f(body))

  /** Attach an extended attribute of type `A` to this request.
    *
    * @tparam A
    *   the new attribute type
    * @param newAttr
    *   the new attribute
    * @return
    *   a request identical to this one, but with an additional extended attribute
    * @see
    *   [[compose.http.attributes]]
    */
  def withAttr[A](newAttr: Attr[A, NoAttrs.type]): Request[Body, Attr[A, Attrs]] =
    this.copy[Body, Attr[A, Attrs]](
      extendedAttributes = newAttr.copy[A, Attrs](rest = extendedAttributes)
    )

}

object Request {

  /** Read a [[Request]] from an input stream.
    *
    * The start line and headers are assumed to be encoded in ISO-8859-1 (per RFC 2616). No attempt
    * is made to handle encoded words per RFC 2047, but any such words are preserved.
    *
    * This returns a request whose body is an [[java.io.InputStream]] (the same one passed in, with
    * the start line and headers having already been read) and no extended attributes.
    *
    * @param inputStream
    *   the stream to read a request from
    * @return
    *   the request read from the stream
    * @see
    *   RFC 2616: [[https://tools.ietf.org/html/rfc2616]]
    * @see
    *   RFC 2047: [[https://tools.ietf.org/html/rfc2047]]
    */
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
