package compose.http.attributes

class AttrMapSpec extends compose.Spec {
  object TestIntKey extends AttrKey[Int]("test int key")
  object TestIntKey2 extends AttrKey[Int]("test int key 2")
  object TestStringKey extends AttrKey[String]("test string key")
  object TestIntKeySameName extends AttrKey[Int]("test int key")
  object TestIntKey2SameName extends AttrKey[Int]("test int key 2")

  val map =
    AttrMap.empty
      .+(TestIntKey -> 1)
      .+(TestStringKey -> "found")
      .+(TestIntKeySameName -> 100)
      .+(TestIntKey2SameName -> 200)

  "Attribute maps" should {
    "find the correct value when the key is present" in {
      map.get(TestIntKey) should equal(Some(1))
    }

    "fail to find the correct value when the key isn't present" in {
      map.get(TestIntKey2) should equal(None)
    }

    "overwrite a key with the + operator" in {
      (map + (TestIntKey -> 123)).get(TestIntKey) should equal(Some(123))
    }

    "overwrite a key with the :@: operator" in {
      ((TestStringKey -> "overwritten") :@: map).get(TestStringKey) should equal(Some("overwritten"))
    }

    "remove a key with the - operator" in {
      val removed = (map - TestIntKeySameName)

      removed.get(TestIntKey) shouldBe a[Some[_]]
      removed.get(TestIntKeySameName) should equal(None)
    }

    "not evaluate the default value if it's not needed" in {
      var sentinel: String = "good"

      map.getOrElse(
        TestIntKey, {
          sentinel = "bad"
          4
        },
      ) shouldNot equal(4)

      sentinel should equal("good")
    }

    "combine correctly" in {
      val map2 = AttrMap.empty + (TestIntKey -> 11) + (TestIntKey2 -> 22)

      val map12 = map ++ map2
      map12.get(TestIntKey) should equal(Some(11))
      map12.get(TestIntKey2) should equal(Some(22))
      map12.get(TestStringKey) should equal(Some("found"))

      val map21 = map2 ++ map
      map21.get(TestIntKey) should equal(Some(1))
      map21.get(TestIntKey2) should equal(Some(22))
      map21.get(TestStringKey) should equal(Some("found"))
    }
  }

  "The :@: extension operator" should {
    "construct an AttrMap" in {
      ((TestIntKey -> 1) :@: (TestIntKey2 -> 2)) shouldBe an[AttrMap]
    }

    "take the lefthand value when there are duplicate keys" in {
      val conflictMap = (TestStringKey -> "left") :@: (TestStringKey -> "right")
      conflictMap.get(TestStringKey) should equal(Some("left"))
    }
  }

  "Extractors" should {
    "find a value when it's present" in {
      val check = map match {
        case TestStringKey(str) => Some(str)
        case _                  => None
      }

      check should equal(Some("found"))
    }

    "fail to find a value when it's not" in {
      val check = map match {
        case TestIntKey2(i) => Some(i)
        case _              => None
      }

      check should equal(None)
    }

    "find multiple values when they're both present" in {
      val check = map match {
        case TestStringKey(str) :@: TestIntKey(i) => Some(str -> i)
        case _                                    => None
      }

      check should equal(Some("found" -> 1))
    }

    "fail to find multiple values if either one is not present" in {
      val check = map match {
        case TestStringKey(str) :@: TestIntKey2(i) => Some(str -> i)
        case _                                     => None
      }

      check should equal(None)
    }
  }
}
