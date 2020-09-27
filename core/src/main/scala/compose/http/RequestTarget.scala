package compose.http

import java.net.URI
import io.lemonlabs.uri.{
  Authority => LLAuthority,
  AbsoluteUrl,
  RelativeUrl,
  AbsoluteOrEmptyPath,
  UserInfo,
  Host,
  UrlPath,
  QueryString,
}
import io.lemonlabs.uri.encoding.percentEncode

/** Common supertype for classes representing different types of targets for HTTP requests.
  *
  * The target types are:
  *   - [[RequestTarget.Path]]
  *   - [[RequestTarget.Absolute]]
  *   - [[RequestTarget.Authority]]
  *   - [[RequestTarget.Asterisk]]
  *
  * `Path` is by far the cmost common.
  */
sealed trait RequestTarget

object RequestTarget {

  /** An absolute path with optional query string. This is the most common target type.
    *
    * @param path
    *   the requested path, without the query string
    * @param queryParams
    *   the query parameters. Take care when using `getOrElse`: any parameter provided without a
    *   value (e.g., `GET /foo?bar`) will be present in the queryParams map, but with an empty
    *   sequence as its value (e.g., `"bar" -> Seq.empty[String]`).
    */
  final case class Path(path: String, queryParams: Map[String, Seq[String]]) extends RequestTarget {

    def isRoot: Boolean = (path == "") || (path == "/")

    /** Construct a string that expresses the same path as this.
      *
      * Note that because order of arguments is somewhat arbitrary, this may not produce the exact
      * same string as the original request.
      *
      * @return
      *   a string representation of this path
      */
    override lazy val toString: String = UrlPath.parseOption(path) match {
      case Some(urlPath) =>
        RelativeUrl(
          urlPath,
          QueryString(
            queryParams.iterator.flatMap {
              case (key, valueSeq) =>
                if (valueSeq.isEmpty) Seq(key -> None)
                else valueSeq.map(value => (key -> Some(value)))
            }.toVector
          ),
          None,
        ).toString()
      case None => (percentEncode -- '/').encode(path, "UTF-8")
    }

  }

  /** An absolute URL. Generally only used when the client expects the server to act as a proxy.
    *
    * @param uri
    *   the URI requested
    */
  final case class Absolute(uri: URI) extends RequestTarget {
    override lazy val toString: String = uri.toString()
  }

  /** A URL authority segment. This must be combined with the [[Method.Connect]] method.
    *
    * @param host
    *   the authority hostname
    * @param user
    *   the authority username, if any
    * @param password
    *   the authority password, if any. If `user` is `None`, this is ignored.
    * @param port
    *   the authority port, if any
    */
  final case class Authority(
    host: String,
    user: Option[String],
    password: Option[String],
    port: Option[Int],
  ) extends RequestTarget {

    override lazy val toString: String =
      LLAuthority(user.map(u => UserInfo(u, password)), Host.parse(host), port).toString()

  }

  /** A target indicating the entire server. Generally used with the [[Method.Options]] method.
    * Represented by a literal `*`.
    */
  case object Asterisk extends RequestTarget {
    override val toString: String = "*"
  }

  /** Parse an HTTP request target string according to RFC 2616.
    *
    *   1. If the string is exactly, `"*"`, return [[Asterisk]].
    *   1. If the string is an absolute URL (scheme, host, and path), return an [[Absolute]].
    *   1. If the string is a URL authority (host, optional username, password, and port), return an
    *      [[Authority]].
    *   1. If the string is an absolute path, optionally with a query string, return a [[Path]].
    *   1. If none of the above match, return the whole string as the path component of a [[Path]]
    *      with an empty query string, and let the application figure out how to interpret it.
    *
    * @param target
    *   a target string from an HTTP request
    * @return
    *   a parsed request target
    * @see
    *   RFC 2616: [[https://tools.ietf.org/html/rfc2616]]
    */
  def parse(target: String): RequestTarget = {
    val trimmed = target.trim()

    // If the target is just "*", then it's an Asterisk
    if (trimmed == Asterisk.toString) {
      Asterisk
    } else {
      // If not, but if we can parse it as an absolute url, then it's an Absolute
      AbsoluteUrl.parseOption(trimmed) match {
        case Some(_) => Absolute(new URI(trimmed))
        case None => {
          // If not, but if we can parse it as an authority, then it's an Authority
          LLAuthority.parseOption(trimmed) match {
            case Some(auth) => Authority(auth.host.toString(), auth.user, auth.password, auth.port)
            case None => {
              // If none of the above apply, it's a Path.
              // If we can extract an absolute path and a query string, do that; if not, just assume the whole thing is a path
              RelativeUrl.parseOption(trimmed) match {
                case Some(RelativeUrl(path: AbsoluteOrEmptyPath, query, _)) =>
                  Path(path.toString(), query.paramMap)
                case _ => Path(trimmed, Map.empty[String, Seq[String]])
              }
            }
          }
        }
      }
    }
  }

}
