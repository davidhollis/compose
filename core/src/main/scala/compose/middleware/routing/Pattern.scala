package compose.middleware.routing

import compose.http.Request
import compose.http.RequestTarget
import compose.Application

trait Pattern[Body] extends (Request[Body] => Pattern.MatchResult[Body]) {
  def |->(app: Application[Body]): Rule[Body] = new PatternRule[Body](this, app)
}

class UnconditionalPattern[Body] extends Pattern[Body] {
  def apply(req: Request[Body]): Pattern.MatchResult[Body] = Pattern.Match(req)
}

class RootPathPattern[Body] extends Pattern[Body] {

  def apply(req: Request[Body]): Pattern.MatchResult[Body] =
    req match {
      case Request(_, _, path: RequestTarget.Path, _, _, _) if path.isRoot => Pattern.Match(req)
      case _                                                               => Pattern.NoMatch
    }

}

object Pattern {
  sealed trait MatchResult[+Body]

  case object NoMatch extends MatchResult[Nothing]
  case class Match[+Body](enrichedRequest: Request[Body]) extends MatchResult[Body]
}
