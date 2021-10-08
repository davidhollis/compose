package compose.middleware.util

import scala.concurrent.Future
import compose.http.Request
import compose.http.Response
import compose.Application

class Immediate[-Body](app: Request[Body] => Response) extends Application[Body] {
  def apply(request: Request[Body]): Future[Response] = Future.successful[Response](app(request))
}

object Immediate {

  def apply[Body](
    app: Request[Body] => Response
  ): Application[Body] =
    new Immediate(app)

}
