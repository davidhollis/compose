package compose.middleware.routing

import compose.http.{ Request, RequestTarget }
import RequestTarget._

package object patterns {

  def unconditional[Body]: Pattern[Body] =
    Pattern[Body] {
      case req => Pattern.Match(req)
    }

  def root[Body]: Pattern[Body] =
    Pattern[Body] {
      case req @ Request(_, _, path: Path, _, _, _) if path.isRoot =>
        Pattern.Match(req)
    }

}
