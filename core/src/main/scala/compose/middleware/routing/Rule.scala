package compose.middleware.routing

import scala.concurrent.Future

import compose.http.{ Request, Response }
import compose.http.Status.NotFound
import compose.rendering.implicits._
import compose.Application

trait Rule[Body] extends Application[Body] {
  def orElse(alternateRule: Rule[Body]): Rule[Body]
}

class NoMatchRule[Body] private[routing] () extends Rule[Body] {

  def apply(request: Request[Body]): Future[Response] =
    Future.successful(
      Response(
        s"Not found: ${request.method} ${request.target}",
        status = NotFound,
      )
    )

  def orElse(alternateRule: Rule[Body]): Rule[Body] = this

}

class PatternRule[Body] private[routing] (
  pattern: Pattern[Body],
  app: Application[Body],
  alternateRule: Rule[Body] = new NoMatchRule[Body],
) extends Rule[Body] {

  def apply(request: Request[Body]): Future[Response] =
    pattern(request) match {
      case Pattern.Match(updatedRequest) => app(updatedRequest)
      case Pattern.NoMatch               => alternateRule(request)
    }

  def orElse(newAlternateRule: Rule[Body]): Rule[Body] =
    new PatternRule[Body](pattern, app, newAlternateRule)

}
