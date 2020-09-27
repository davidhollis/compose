package compose.http.attributes

class AttrMap(underlying: Map[Any, Any] = Map.empty) {
  def contains[T](key: AttrKey[T]): Boolean = underlying.contains(key)
  def apply[T](key: AttrKey[T]): T = underlying(key).asInstanceOf[T]
  def get[T](key: AttrKey[T]): Option[T] = underlying.get(key).asInstanceOf[Option[T]]
  def getOrElse[T, T1 >: T](key: AttrKey[T], default: => T1): T1 = get(key).getOrElse[T1](default)
  def +[T](kv: (AttrKey[T], T)): AttrMap = new AttrMap(underlying + kv)
  def -[T](key: AttrKey[T]): AttrMap = new AttrMap(underlying - key)
  def :@:[T](kv: (AttrKey[T], T)): AttrMap = this + kv
}

object AttrMap {
  def apply(): AttrMap = new AttrMap
}

abstract class AttrKey[+T](override val toString: String) {
  def unapply(map: AttrMap): Option[T] = map.get(this)
}

object :@: {
  def unapply(map: AttrMap): Option[(AttrMap, AttrMap)] = Some((map, map))
}
