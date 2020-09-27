package compose.http.attributes

/** An immutable, heterogeneously-typed map with static type checks on access.
  *
  * While the map itself can contain any kind of values, keys are of type [[AttrKey]] and carry
  * type information for their associated values (e.g., a key of type `AttrKey[Int]` must be
  * associated with a value of type `Int`).
  */
class AttrMap private[attributes] (private[AttrMap] val underlying: Map[Any, Any] = Map.empty) {

  /** Check whether the map contains a value for the given key.
    *
    * @tparam T The type of the kev-value pair
    * @param key The key to check for
    */
  def contains[T](key: AttrKey[T]): Boolean = underlying.contains(key)

  /** Get the value associated with the given key.
    *
    * @tparam T The type of the kev-value pair
    * @param key The key whose value to extract
    * @return The value
    * @throws java.util.NoSuchElementException if the key is not found
    */
  @SuppressWarnings(
    Array(
      // insertion of keys is typechecked and the underlying map is private, so casting on
      // extraction should be safe
      "scalafix:DisableSyntax.asInstanceOf"
    )
  )
  def apply[T](key: AttrKey[T]): T = underlying(key).asInstanceOf[T]

  /** Get the value associated with the given key as an `Option`.
    *
    * @tparam T The type of the kev-value pair
    * @param key The key whose value to extract
    * @return `Some(` the value `)`, or `None` if the key is not found
    */
  @SuppressWarnings(
    Array(
      // insertion of keys is typechecked and the underlying map is private, so casting on
      // extraction should be safe
      "scalafix:DisableSyntax.asInstanceOf"
    )
  )
  def get[T](key: AttrKey[T]): Option[T] = underlying.get(key).asInstanceOf[Option[T]]

  /** Get the value associated with the given key, or the default value if the key is not present.
    *
    * @tparam T The type of the kev-value pair
    * @param key The key whose value to extract
    * @param default The default value, which is not evaluated if it's not needed
    * @return The value associated with the key, or the default value if the key is not found
    */
  def getOrElse[T, T1 >: T](key: AttrKey[T], default: => T1): T1 = get(key).getOrElse[T1](default)

  /** Add a new key-value pair to this map, overwriting any existing value assocaited with the
    * key.
    *
    * @tparam T The type of the kev-value pair
    * @param kv The new key-value pair
    * @return A new `AttrMap` with the same contents as this one, plus the given key-value pair
    */
  def +[T](kv: (AttrKey[T], T)): AttrMap = new AttrMap(underlying + kv)

  /** Remove the key-value pair with the given key, if present.
    *
    * @tparam T The type of the kev-value pair
    * @param key The key whose pair to remove
    * @return A new `AttrMap` that does not contain any pair with the given key
    */
  def -[T](key: AttrKey[T]): AttrMap = new AttrMap(underlying - key)

  /** Right-associative operator to add a key-value pair to this map.
    *
    * This enables constructions like
    *
    * {{{
    * val intKey: AttrKey[Int] = ???
    * val stringKey: AttrKey[String] = ???
    * val attrMap: AttrMap = ???
    * (stringKey -> "seven") :@: (intKey -> 7) :@: attrMap
    * }}}
    *
    * This syntax is mostly supported as a parallel to the [[compose.http.attributes.:@:]]
    * extractor. Using the [[+]] operator should generally be more clear.
    *
    * @tparam T The type of the kev-value pair
    * @param kv The new key-value pair
    * @return A new `AttrMap` with the same contents as this one, plus the given key-value pair
    */
  def :@:[T](kv: (AttrKey[T], T)): AttrMap = this + kv

  /** Combine this map and another `AttrMap` into a new map.
    *
    * In the case of duplicate keys, the new map will have the value from the righthand map.
    *
    * @param otherMap The other map to combine with this.
    * @return A new map containing the key-value pairs of both maps, deferring to `otherMap` in the
    *   case of a conflict
    */
  def ++(otherMap: AttrMap): AttrMap = new AttrMap(underlying ++ otherMap.underlying)
}

/** Constructors for [[AttrMap]]s */
object AttrMap {

  /** An empty [[AttrMap]] */
  lazy val empty: AttrMap = new AttrMap

  /** Get an empty [[AttrMap]]
    *
    * @return an empty map
    */
  def apply(): AttrMap = empty
}

/** Ancestor class of an key in an [[AttrMap]].
  *
  * @tparam T the type of value this key must be associated with
  * @param toString A human-friendly string representation of this key. Note that keys are compared
  *   by object identity, not by name (so two keys with the same name are not necessarily equal).
  */
abstract class AttrKey[+T](override val toString: String) {

  /** An extractor to enable getting values out of an [[AttrMap]] using pattern matching.
    *
    * @param map an attribute map
    * @return A value associated with this key in `map`, or `None` if this key isn't present
    *   in `map`.
    */
  def unapply(map: AttrMap): Option[T] = map.get(this)
}

/** An extractor combinator to enable extracting multiple values from the same [[AttrMap]]
  *
  * @example Extracting multiple values from the same [[AttrMap]]
  * {{{
  * object ExampleIntKey[Int]("example int key")
  * object ExampleStringKey[String]("example string key")
  * object ExampleBooleanKey[Boolean]("example boolean key")
  * val map = (ExampleIntKey -> 2) :@: (ExampleBooleanKey -> false) :@: (ExampleStringKey -> "two")
  *
  * map match {
  *   case ExampleStringKey(s) :@: ExampleIntKey(i) => println(s"found \$s and \$i")
  *   case _                                        => println("nope")
  * }
  * // prints "found two and 2"
  * }}}
  */
object :@: {

  /** Unconditionally extract `map` into `(map, map)` to enable multiple matches against the same
    * map.
    *
    * @param map The map to duplicate
    * @return `(map, map)`, unconditionally
    */
  def unapply(map: AttrMap): Option[(AttrMap, AttrMap)] = Some((map, map))
}
