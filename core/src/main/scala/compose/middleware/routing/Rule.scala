package compose.middleware.routing

import scala.concurrent.Future

import compose.http.attributes.AttrList
import compose.http.{ Request, Response }
import compose.http.Status.NotFound
import compose.rendering.implicits._
import compose.Application

trait Rule[-Body, -Attrs <: AttrList] extends Application[Body, Attrs] {
  def orElse[BB <: Body, AA <: Attrs](alternateRule: Rule[BB, AA]): Rule[BB, AA]
}

class NoMatchRule[-Body, -Attrs <: AttrList] private[routing] () extends Rule[Body, Attrs] {

  def apply(request: Request[Body, Attrs]): Future[Response] =
    Future.successful(
      Response(
        s"Not found: ${request.method} ${request.target}",
        status = NotFound,
      )
    )

  def orElse[BB <: Body, AA <: Attrs](alternateRule: Rule[BB, AA]): Rule[BB, AA] = this

}

class PatternRule[-Body, -Attrs <: AttrList, -InnerAttrs <: AttrList] private[routing] (
  pattern: Pattern[Body, Attrs],
  app: Application[Body, InnerAttrs],
  augment: RoutingParams => Attrs => InnerAttrs,
  alternateRule: Rule[Body, Attrs] = new NoMatchRule,
) extends Rule[Body, Attrs] {

  def apply(request: Request[Body, Attrs]): Future[Response] =
    pattern(request) match {
      case Pattern.Match(matchParams) => app(request.mapAttrs[InnerAttrs](augment(matchParams)))
      case Pattern.NoMatch            => alternateRule(request)
    }

  def orElse[BB <: Body, AA <: Attrs](newAlternateRule: Rule[BB, AA]): Rule[BB, AA] =
    new PatternRule[BB, AA, InnerAttrs](pattern, app, augment, newAlternateRule)

}
