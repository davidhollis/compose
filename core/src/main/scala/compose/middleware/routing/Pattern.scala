package compose.middleware.routing

import compose.http.Request
import compose.Application

abstract class Pattern[Body] extends (Request[Body] => Pattern.MatchResult[Body]) {

  val applyPartial: PartialFunction[Request[Body], Pattern.MatchResult[Body]]

  def apply(req: Request[Body]): Pattern.MatchResult[Body] =
    applyPartial.applyOrElse(req, (_: Request[Body]) => Pattern.NoMatch)

  def |->(app: Application[Body]): Rule[Body] = new PatternRule[Body](this, app)
}

object Pattern {

  def apply[Body](pf: PartialFunction[Request[Body], MatchResult[Body]]): Pattern[Body] =
    new Pattern[Body] {
      val applyPartial = pf
    }

  sealed trait MatchResult[+Body]

  case object NoMatch extends MatchResult[Nothing]
  case class Match[+Body](enrichedRequest: Request[Body]) extends MatchResult[Body]
}
