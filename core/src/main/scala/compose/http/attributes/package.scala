package compose.http

/** Support for type-safe extended request attributes */
package object attributes {

  type NoAttrs = NoAttrs.type

  implicit class AttrPairArrow[T](val attrTag: AttrTag[T]) {
    def ~>(value: T): Attr[T, NoAttrs] = Attr(attrTag, value, NoAttrs)
  }

}
