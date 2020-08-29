package compose.http

import scala.io.Source

/** A set of HTTP request or response headers.
  *
  * A header set is a collection of key/value pairs, where keys can be repeated (i.e., keys may have
  * multiple values). Per [[https://tools.ietf.org/html/rfc2616RFC 2616]] (section 4.2), keys are
  * '''case-insentive'''.
  *
  * @constructor
  *   Build a `Headers` directly from its internal representation. Generally, it's recommended to
  *   use [[Headers$.apply]] instead.
  * @param raw
  *   the underlying representation of the headers
  */
case class Headers(raw: Map[String, Seq[String]]) {

  /** Get a value associated with the given key, if present.
    *
    * If multiple values are present for the given key, only one is returned.
    *
    * @param key
    *   the key to look up
    * @return
    *   `Some` of a value associated with the key, or `None` if there is no such value
    */
  def get(key: String): Option[String] = raw.get(key.toLowerCase()).flatMap(_.lastOption)

  /** Get all values associated with the given key.
    *
    * @param key
    *   the key to look up
    * @return
    *   a sequence of all values associated with the key, empty if there are no such values
    */
  def getAll(key: String): Seq[String] = raw.getOrElse(key.toLowerCase(), Seq.empty[String])

  /** An iterator of all key/value pairs in this header set. */
  def iterator: Iterator[(String, String)] =
    raw.iterator.flatMap {
      case (key, allValues) =>
        allValues.map { v => (key, v) }
    }

  /** Remove all values associated with the given key.
    *
    * @param key
    *   the key to remove
    * @return
    *   a new `Headers` with the given key removed
    */
  def remove(key: String): Headers =
    Headers(raw.removed(key.toLowerCase()))

  /** Add the given key/value pair, replacing all other ocurrences of that key.
    *
    * {{{
    * scala> val h = Headers("abc" -> "123", "abc" -> "456", "def" -> "789")
    * val h: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(123, 456), def -> ArraySeq(789)))
    *
    * scala> h.getAll("abc")
    * val res0: Seq[String] = ArraySeq(123, 456)
    *
    * scala> h.replace("abc", "replaced").getAll("abc")
    * val res1: Seq[String] = List(replaced)
    * }}}
    *
    * @param key
    *   the key whose values will be replaced
    * @param value
    *   the new value for that key
    * @return
    *   a new `Headers` with the given key replaced
    */
  def replace(key: String, value: String): Headers =
    Headers(raw.updated(key.toLowerCase(), Seq(value)))

  /** Add all of the given key/value pairs, replacing values as necessary.
    *
    * Whenever a key is present in both this and `pairs`, the returned `Headers` will have all of
    * the values from `pairs` and none of the values from this:
    * {{{
    * scala> val h = Headers("abc" -> "123", "abc" -> "456", "def" -> "789")
    * val h: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(123, 456), def -> ArraySeq(789)))
    *
    * scala> h.replace("abc" -> "321", "ghi" -> "654")
    * val res0: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(321), def -> ArraySeq(789), ghi -> ArraySeq(654)))
    * }}}
    *
    * @param pairs
    *   the key/value pairs to replace
    * @return
    *   a new `Headers` with the given pairs replaced
    */
  def replace(pairs: (String, String)*): Headers =
    Headers(raw ++ Headers(pairs: _*).raw)

  /** Add all the key/value pairs from the given `Headers`, replacing this `Headers`'s values as
    * necessary.
    *
    * Whenever a key is present in both this and `other`, the returned `Headers` will have all the
    * values from `other` and none of the values from this:
    * {{{
    * scala> val h = Headers("abc" -> "123", "abc" -> "456", "def" -> "789")
    * val h: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(123, 456), def -> ArraySeq(789)))
    *
    * scala> h.replace(Headers("abc" -> "321", "ghi" -> "654"))
    * val res0: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(321), def -> ArraySeq(789), ghi -> ArraySeq(654)))
    * }}}
    *
    * @param other
    *   a `Headers` containing the key/value pairs to replace
    * @return
    *   a new `Headers` with the given pairs replaced
    */
  def replace(other: Headers): Headers =
    Headers(raw ++ other.raw)

  /** Add the given key/value pair, appending the value if they key is already present.
    *
    * @param key
    *   the key to add
    * @param value
    *   the value to add
    * @return
    *   a new `Headers` containing the given key/value pair in addition to those from this `Headers`
    */
  def add(key: String, value: String): Headers =
    Headers(
      raw.updated[Seq[String]](
        key.toLowerCase(),
        getAll(key) :+ value,
      )
    )

  /** Add all of the given key/value pairs, appending values as necessary.
    *
    * Whenever a key is present in both this and `pairs`, the returned `Headers` will have all
    * values from both:
    * {{{
    * scala> val h = Headers("abc" -> "123", "abc" -> "456", "def" -> "789")
    * val h: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(123, 456), def -> ArraySeq(789)))
    *
    * scala> h.add("abc" -> "321", "ghi" -> "654")
    * val res0: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(123, 456, 321), def -> ArraySeq(789), ghi -> ArraySeq(654)))
    * }}}
    *
    * @param pairs
    *   the key/value pairs to add
    * @return
    *   a new `Headers` with the given pairs added
    */
  def add(pairs: (String, String)*): Headers =
    Headers((this.iterator ++ pairs).toSeq: _*)

  /** Add all the key/value pairs from the given `Headers`, appending values as necessary.
    *
    * Whenever a key is present in both this and `other`, the returned `Headers` wil have all values
    * from both:
    * {{{
    * scala> val h = Headers("abc" -> "123", "abc" -> "456", "def" -> "789")
    * val h: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(123, 456), def -> ArraySeq(789)))
    *
    * scala> h.add(Headers("abc" -> "321", "ghi" -> "654"))
    * val res0: compose.http.Headers = Headers(HashMap(abc -> ArraySeq(123, 456, 321), def -> ArraySeq(789), ghi -> ArraySeq(654)))
    * }}}
    *
    * @param other
    *   a `Headers` containing the key/value pairs to add
    * @return
    *   a new `Headers` with the given pairs added
    */
  def add(other: Headers): Headers =
    Headers((this.iterator ++ other.iterator).toSeq: _*)

  /** Convert this `Headers` into a string suitable for including in an HTTP response.
    *
    * @return
    *   a string representation of this `Headers`
    */
  def render: String =
    iterator
      .map {
        case (key, value) =>
          s"${Headers.formatKey(key)}: ${value}"
      }
      .mkString("\r\n")

}

object Headers {
  private[http] val encoding: String = "ISO-8859-1"

  /** Construct a `Headers` from a sequence of key/value pairs. This is the primary way of
    * constructing `Headers` objects.
    *
    * @param initial
    *   a sequence of key/value pairs
    * @return
    *   a `Headers` containing those pairs
    */
  def apply(initial: (String, String)*): Headers =
    Headers(
      (for {
        (key, pairs) <- initial.groupBy(_._1.toLowerCase())
      } yield (key -> pairs.map(_._2))).toMap
    )

  /** A `Headers` containing no key/value pairs. */
  lazy val empty: Headers = Headers(Map.empty[String, Seq[String]])

  /** Construct a `Headers` from an HTTP request.
    *
    * `parse` reads lines from `source` until it encounters a blank line. For each line of the form
    * {{{
    * key: value
    * }}}
    * the pair `(key -> value)` is added to the resulting `Headers`. Nonblank lines that are not of
    * that form are ignored.
    *
    * @param source
    *   the source to read the header lines from
    * @return
    *   a `Headers` containing the key/value pairs found in `source`
    */
  def parse(source: Source): Headers =
    Headers(
      source
        .getLines()
        .takeWhile(!_.isEmpty)
        .flatMap { line =>
          val colonIndex = line.indexOf(":")
          if (colonIndex >= 0) {
            val key = line.take(colonIndex).trim
            val value = line.drop(colonIndex + 1).trim
            Some(key -> value)
          } else {
            None
          }
        }
        .toSeq: _*
    )

  private def formatKey(key: String): String = key.split("-").map(_.capitalize).mkString("-")
}
