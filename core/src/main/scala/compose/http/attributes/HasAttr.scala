package compose.http.attributes

import scala.annotation.{ showAsInfix, unused }

/** Evidence that a given attribute list has an element of a specific type.
  *
  * @tparam Attrs
  *   the attribute list type
  * @tparam A
  *   the element type
  */
@showAsInfix
trait HasAttr[Attrs <: AttrList, A]

object HasAttr {

  private def apply[Attrs <: AttrList, A](): HasAttr[Attrs, A] = new HasAttr[Attrs, A] {}

  /** Evidence that an attribute list contains an element of a specific type if the head of the list
    * is an element of that type.
    *
    * @tparam T
    *   the type of the attribute list's head element
    * @tparam Rest
    *   the type of the attribute list's tail
    */
  implicit def headHasAttr[T, Rest <: AttrList]: HasAttr[Attr[T, Rest], T] = HasAttr()

  /** Evidence that an attribute list contains an element of a specific type if we have evidence
    * that the tail contains an attribute of that type.
    *
    * @tparam T
    *   the type of the attribute list's head element
    * @tparam Rest
    *   the type of the attribute list's tail
    * @tparam A
    *   the type we wish to find
    * @param ev
    *   evidence that the tail contains an element of type `A`
    */
  implicit def tailHasAttr[T, Rest <: AttrList, A](
    implicit
    @unused ev: HasAttr[Rest, A]
  ): HasAttr[Attr[T, Rest], A] = HasAttr()

}
