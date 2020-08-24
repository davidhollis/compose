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

sealed trait RequestTarget

object RequestTarget {

  final case class Path(path: String, queryParams: Map[String, Seq[String]]) extends RequestTarget {

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

  final case class Absolute(uri: URI) extends RequestTarget {
    override lazy val toString: String = uri.toString()
  }

  final case class Authority(
    host: String,
    user: Option[String],
    password: Option[String],
    port: Option[Int],
  ) extends RequestTarget {

    override lazy val toString: String =
      LLAuthority(user.map(u => UserInfo(u, password)), Host.parse(host), port).toString()

  }

  case object Asterisk extends RequestTarget {
    override val toString: String = "*"
  }

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
