package compose.http.attributes

trait HasAttr[Attrs <: AttrList, A]

object HasAttr {

  private def apply[Attrs <: AttrList, A](): HasAttr[Attrs, A] = new HasAttr[Attrs, A] {}

  implicit def headHasAttr[T, Rest <: AttrList]: HasAttr[Attr[T, Rest], T] = HasAttr()

  implicit def tailHasAttr[T, Rest <: AttrList, A](
    implicit
    ev: HasAttr[Rest, A]
  ): HasAttr[Attr[T, Rest], A] = HasAttr()

}
