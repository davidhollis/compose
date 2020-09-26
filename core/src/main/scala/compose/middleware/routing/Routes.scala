package compose.middleware.routing

import compose.Application
import compose.http.attributes.AttrList

object Routes {

  def apply[Body, Attrs <: AttrList](rules: Rule[Body, Attrs]*): Application[Body, Attrs] =
    rules.foldLeft[Rule[Body, Attrs]](new NoMatchRule)(_ orElse _)

}
