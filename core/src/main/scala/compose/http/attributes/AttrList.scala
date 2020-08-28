package compose.http.attributes

sealed trait AttrList {
  def apply[V](tag: AttrTag[V]): Option[V] = tag.unapply(this)
}

final case class Attr[+T, +Rest <: AttrList](tag: AttrTag[T], value: T, rest: Rest) extends AttrList

case object NoAttrs extends AttrList

abstract class AttrTag[+T](override val toString: String) {

  def unapply(list: AttrList): Option[T] =
    list match {
      case Attr(tag, value: T, _) if tag == this => Some(value)
      case Attr(_, _, rest)                      => unapply(rest)
      case NoAttrs                               => None
    }

}
