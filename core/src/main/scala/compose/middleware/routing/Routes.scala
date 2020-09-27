package compose.middleware.routing

import compose.Application

object Routes {

  def apply[Body](rules: Rule[Body]*): Application[Body] =
    rules.foldLeft[Rule[Body]](new NoMatchRule)(_ orElse _)

}
