package compose.http.attributes

class AttrListSpec extends compose.Spec {
  object IntTag1 extends AttrTag[Int]("test int tag 1")
  object IntTag2 extends AttrTag[Int]("test int tag 2")
  object SameNameIntTag extends AttrTag[Int]("test int tag 1")
  object StringTag extends AttrTag[String]("test string tag")
  object SameNameStringTag extends AttrTag[String]("test int tag 1")

  "Attribute lookup" should {
    val attr = IntTag1 ~> 12

    "find an attribute with the right tag" when {
      "the structure is simple" in {
        attr(IntTag1) should equal(Some(12))
      }

      "the structure is complex" in {
        val complexAttr = Attr(IntTag2, 40, Attr(StringTag, "forty", Attr(IntTag1, 12, NoAttrs)))
        complexAttr(IntTag1) should equal(Some(12))
      }
    }

    "fail to find an attribute with the wrong tag" when {
      "the tag has the wrong type" in {
        attr(StringTag) should equal(None)
      }

      "the tag has the wrong name" in {
        attr(IntTag2) should equal(None)
      }

      "the tag has the same name but a different object identity" in {
        attr(SameNameIntTag) should equal(None)
      }

      "the tag has the same name but a different type" in {
        attr(SameNameStringTag) should equal(None)
      }
    }
  }

  "Type assertions" should {
    "compile" when {
      "looking for a type that is in the attribute structure" in {
        """
          |import compose.http.attributes._
          |type TestAttrType = Attr[Int, Attr[String, Attr[Boolean, NoAttrs]]]
          |val verify1 = implicitly[TestAttrType HasAttr Int]
          |val verify2 = implicitly[TestAttrType HasAttr String]
          |val verify3 = implicitly[TestAttrType HasAttr Boolean]
        """.stripMargin should compile
      }
    }

    "not compile" when {
      "looking for a type that isn't in the attribute structure" in {
        """
          |import compose.http.attributes._
          |type TestAttrType = Attr[Int, Attr[String, Attr[Boolean, NoAttrs]]]
          |val falsify = implicitly[TestAttrType HasAttr Float]
        """.stripMargin shouldNot typeCheck
      }

      "looking for NoAttrs" in {
        """
          |import compose.http.attributes._
          |type TestAttrType = Attr[Int, Attr[String, Attr[Boolean, NoAttrs]]]
          |val falsify = implicitly[TestAttrType HasAttr NoAttrs]
        """.stripMargin shouldNot typeCheck
      }
    }
  }
}
