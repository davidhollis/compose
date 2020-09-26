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

  "Updates on attribute lists" should {
    "modify the correct attribute" when {
      "the type is unique and the selected tag is the head" in {
        val attrs = Attr(IntTag1, 19, Attr(StringTag, "nineteen", NoAttrs))
        val updated =
          implicitly[Attr[Int, Attr[String, NoAttrs]] HasAttr Int]
            .update(attrs, IntTag1, (i => (i * 100) + 99))

        updated(IntTag1) should equal(Some(1999))
      }

      "the type is unique and the selected tag is in the tail" in {
        val attrs = Attr(IntTag1, 19, Attr(StringTag, "nineteen", NoAttrs))
        val updated =
          implicitly[Attr[Int, Attr[String, NoAttrs]] HasAttr String]
            .update(attrs, StringTag, century => century + " ninety-nine")

        updated(StringTag) should equal(Some("nineteen ninety-nine"))
      }

      "the type is not unique and the selected tag is first" in {
        val attrs = Attr(IntTag1, 39, Attr(IntTag2, 43, NoAttrs))
        val updated =
          implicitly[Attr[Int, Attr[Int, NoAttrs]] HasAttr Int].update(attrs, IntTag1, i => i - 12)

        updated(IntTag1) should equal(Some(27))
        updated(IntTag2) should equal(Some(43))
      }

      "the type is not unique and the selected tag is not first" in {
        val attrs = Attr(IntTag1, 39, Attr(IntTag2, 43, NoAttrs))
        val updated =
          implicitly[Attr[Int, Attr[Int, NoAttrs]] HasAttr Int].update(attrs, IntTag2, i => i - 12)

        updated(IntTag1) should equal(Some(39))
        updated(IntTag2) should equal(Some(31))
      }

      "the tag is not unique" in {
        // Specifically, if the tag is not unique, `update` should modfiy only the leftmost (most
        // recently added) value with the selected tag. This is because only that one is selectable
        // using AttrTag[T].unapply
        val attrs = Attr(IntTag1, 39, Attr(IntTag1, 43, NoAttrs))
        val updated =
          implicitly[Attr[Int, Attr[Int, NoAttrs]] HasAttr Int].update(attrs, IntTag1, i => i * 5)

        updated should equal(Attr(IntTag1, 195, Attr(IntTag1, 43, NoAttrs)))
      }
    }

    "fail to modify anything" when {
      "the type is not present" in {
        // It shouldn't even compile if a value of the selected tag's type isn't there
        """
          |import compose.http.attributes._
          |object FloatTag extends AttrTag[Float]("test float tag")
          |object BooleanTag extends AttrTag[Boolean]("test boolean tag")
          |object CharTag extends AttrTag[Char]("test char tag")
          |val attrs = Attr(FloatTag, 1.0f, Attr(BooleanTag, true, NoAttrs))
          |val updated =
          |  implicitly[Attr[Float, Attr[Boolean, NoAttrs]] HasAttr Char].update(attrs, CharTag, identity[Char])
        """.stripMargin shouldNot typeCheck
      }

      "the tag is not present" in {
        object FloatTag1 extends AttrTag[Float]("test float tag 1")
        object FloatTag2 extends AttrTag[Float]("test float tag 2")
        object FloatTag3 extends AttrTag[Float]("test float tag 3")
        object FloatTag4 extends AttrTag[Float]("test float tag 4")

        val attrs = Attr(FloatTag1, 1.0f, Attr(FloatTag2, 2.0f, Attr(FloatTag3, 3.0f, NoAttrs)))
        val updated = implicitly[Attr[Float, Attr[Float, Attr[Float, NoAttrs]]] HasAttr Float]
          .update(attrs, FloatTag4, _ => 0.0f)

        updated should equal(attrs)
      }
    }
  }
}
