package compose.http.attributes

import scala.annotation.{ implicitNotFound, showAsInfix }

/** Evidence that a given attribute list has an element of a specific type.
  *
  * @tparam Attrs
  *   the attribute list type
  * @tparam A
  *   the element type
  */
@showAsInfix
@implicitNotFound("Could not prove that ${Attrs} contains a value of type ${A}")
trait HasAttr[Attrs <: AttrList, A] {
  def update(list: Attrs, tag: AttrTag[A], op: A => A): Attrs
}

trait HasAttrLowPriorityImplicits {

  /** Evidence that an attribute list contains an element of a specific type if the head of the list
    * is an element of that type.
    *
    * @tparam T
    *   the type of the attribute list's head element
    * @tparam Rest
    *   the type of the attribute list's tail
    */
  implicit def headHasAttr[T, Rest <: AttrList]: HasAttr[Attr[T, Rest], T] =
    new HasAttr[Attr[T, Rest], T] {

      def update(list: Attr[T, Rest], tag: AttrTag[T], op: T => T): Attr[T, Rest] =
        if (list.tag == tag)
          list.copy(value = op(list.value))
        else
          list

    }

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
    ev: Rest HasAttr A
  ): HasAttr[Attr[T, Rest], A] =
    new HasAttr[Attr[T, Rest], A] {

      def update(list: Attr[T, Rest], tag: AttrTag[A], op: A => A): Attr[T, Rest] =
        list.copy(rest = ev.update(list.rest, tag, op))

    }

}

trait HasAttrHighPriorityImplicits extends HasAttrLowPriorityImplicits {

  implicit def headAndTailBothHaveAttr[T, Rest <: AttrList](
    implicit
    ev: Rest HasAttr T
  ): HasAttr[Attr[T, Rest], T] =
    new HasAttr[Attr[T, Rest], T] {

      def update(list: Attr[T, Rest], tag: AttrTag[T], op: T => T): Attr[T, Rest] =
        if (list.tag == tag)
          list.copy(value = op(list.value))
        else
          list.copy(rest = ev.update(list.rest, tag, op))

    }

}

object HasAttr extends HasAttrHighPriorityImplicits
