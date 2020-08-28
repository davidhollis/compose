package compose.http

package object attributes {

  type NoAttrs = NoAttrs.type

  implicit class AttrPairArrow[T](val attrTag: AttrTag[T]) {
    def ~>(value: T): Attr[T, NoAttrs] = Attr(attrTag, value, NoAttrs)
  }

}
