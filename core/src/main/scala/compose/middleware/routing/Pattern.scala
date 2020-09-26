package compose.middleware.routing

import compose.http.attributes.AttrList
import compose.http.Request
import compose.http.attributes.AttrTag

trait Pattern[-Body, -Attrs <: AttrList] extends (Request[Body, Attrs] => Pattern.MatchResult)

object Pattern {
  sealed trait MatchResult

  case object NoMatch extends MatchResult
  case class Match(params: RoutingParams) extends MatchResult
}

sealed trait RoutingParam

object RoutingParams extends AttrTag[RoutingParams]("routing parameters") {

  def merge(
    params1: RoutingParams,
    params2: RoutingParams,
  ): RoutingParams = {
    (
      for {
        key <- params1.keySet union params2.keySet
      } yield (key -> (params1.getOrElse(key, Seq.empty) ++ params2.getOrElse(key, Seq.empty)))
    ).toMap
  }

}
