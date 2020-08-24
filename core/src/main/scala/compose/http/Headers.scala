package compose.http

import scala.io.Source

case class Headers(
  raw: Map[String, Seq[String]],
) {
  def get(key: String): Option[String] = raw.get(key.toLowerCase()).flatMap(_.lastOption)

  def getAll(key: String): Seq[String] = raw.getOrElse(key.toLowerCase(), Seq.empty[String])

  def iterator: Iterator[(String, String)] =
    raw.iterator.flatMap { case (key, allValues) =>
      allValues.map { v => (key, v) }
    }

  def remove(key: String): Headers =
    Headers(raw.removed(key.toLowerCase()))

  def replace(key: String, value: String): Headers =
    Headers(raw.updated(key.toLowerCase(), Seq(value)))

  def replace(pairs: (String, String)*): Headers =
    Headers(raw ++ Headers(pairs: _*).raw)

  def replace(other: Headers): Headers =
    Headers(raw ++ other.raw)

  def add(key: String, value: String): Headers =
    Headers(raw.updated[Seq[String]](
      key.toLowerCase(),
      getAll(key) :+ value
    ))

  def add(pairs: (String, String)*): Headers =
    Headers((this.iterator ++ pairs).toSeq: _*)
  
  def add(other: Headers): Headers =
    Headers((this.iterator ++ other.iterator).toSeq: _*)

  def render: String = iterator.map { case (key, value) =>
    s"${Headers.formatKey(key)}: ${value}"
  }.mkString("\r\n")
}

object Headers {
  private[http] val encoding: String = "ISO-8859-1"

  def apply(initial: (String, String)*): Headers = Headers(
    (for {
      (key, pairs) <- initial.groupBy(_._1.toLowerCase())
    } yield (key -> pairs.map(_._2))).toMap
  )

  lazy val empty: Headers = Headers(Map.empty[String, Seq[String]])

  def parse(source: Source): Headers = Headers(
    source.getLines().takeWhile(!_.isEmpty).flatMap { line =>
      val colonIndex = line.indexOf(":")
      if (colonIndex >= 0) {
        val key = line.take(colonIndex).trim
        val value = line.drop(colonIndex + 1).trim
        Some(key -> value)
      } else {
        None
      }
    }.toSeq: _*
  )

  def formatKey(key: String): String = key.split("-").map(_.capitalize).mkString("-")
}