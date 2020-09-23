package compose.http.attributes

import scala.annotation.tailrec

/** A typed linked of extended attributes on a [[Request]] */
sealed trait AttrList {

  /** Look up a value in this attribute list by tag.
    *
    * If the tag is present in the list, this will return the most recently added value for the tag.
    * If the tag isn't in the list, this will return `None`.
    *
    * @tparam V
    *   the type of the value to extract
    * @param tag
    *   the tag associated with the value to extract
    * @return
    *   the most recent value with the given tag, if any
    */
  def apply[V](tag: AttrTag[V]): Option[V] = tag.unapply(this)
}

/** An element of an attribute list ([[AttrList]]).
  *
  * @tparam T
  *   the type of this element's value
  * @tparam Rest
  *   the types of the rest of the list
  * @param tag
  *   this element's tag
  * @param value
  *   this element's value
  * @param rest:
  *   the remainder of the list
  */
final case class Attr[+T, +Rest <: AttrList](tag: AttrTag[T], value: T, rest: Rest)
    extends AttrList {

  /** Create a copy of this element and place it at the head of another attribute list.
    *
    * @tparam NewList
    *   the type of the new list
    * @param newList
    *   the list this element will be prepended to
    * @return
    *   a copy of this element placed at the head of `newList`
    */
  def addTo[NewList <: AttrList](newList: NewList): Attr[T, NewList] =
    this.copy[T, NewList](rest = newList)

}

/** An empty [[AttrList]]. */
case object NoAttrs extends AttrList

/** A tag used to identify elements of an [[AttrList]]
  *
  * Generally, rather than subclassing this, new tags should be created as singletons which extend
  * it:
  * {{{
  * object IdempotencyToken extends AttrTag[Long]("idempotency token")
  * }}}
  *
  * @tparam T
  *   the type of value this tag can be associated with
  * @param toString
  *   a human-readable name for this tag
  */
abstract class AttrTag[+T](override val toString: String) {

  /** Extract a value associated with this tag from the given attribute list.
    *
    * @param list
    *   the list to search
    * @return
    *   the first element associated with this tag, if any
    */
  @tailrec
  final def unapply(list: AttrList): Option[T] =
    list match {
      case Attr(tag, value: T @unchecked, _) if tag == this => {
        // @unchecked because the compiler can't verify the type of `value`, but we still know it's a `T`
        Some(value)
      }
      case Attr(_, _, rest) => unapply(rest)
      case NoAttrs          => None
    }

}
