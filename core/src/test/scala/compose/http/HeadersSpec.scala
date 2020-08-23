package compose.http

import scala.io.Source

class HeadersSpec extends compose.Spec {
  "A Headers instance" should {
    "ignore case of header names" in {
      val headers = Headers("TEST-HEADER" -> "TEST VALUE")
      headers.get("test-header") should equal (Some("TEST VALUE"))
    }

    "get all values for a header" in {
      val headers = Headers(
        "test-header" -> "value 1",
        "test-header" -> "value 2",
        "test-header" -> "value 3",
      )

      headers.getAll("test-header") should contain theSameElementsAs Seq("value 1", "value 2", "value 3")
    }

    "remove all values for a header" in {
      val headers = Headers(
        "test-removed" -> "value 1",
        "test-removed" -> "value 2",
        "test-remains" -> "value 3",
        "test-remains" -> "value 4",
      ).remove("test-removed")

      headers.getAll("test-removed") should equal (Seq.empty[String])
      headers.getAll("test-remains") should contain theSameElementsAs Seq("value 3", "value 4")
    }

    "replace one or more headers" in {
      val headers = Headers(
        "test-replaced-1" -> "value 1",
        "test-replaced-1" -> "value 2",
        "test-replaced-2" -> "value 3",
        "test-replaced-2" -> "value 4",
        "test-remains" -> "value 5",
      )

      val replaced1 = headers.replace("test-replaced-1", "value 6")
      replaced1.getAll("test-replaced-1") should contain theSameElementsAs Seq("value 6")
      replaced1.getAll("test-replaced-2") should contain theSameElementsAs Seq("value 3", "value 4")
      replaced1.getAll("test-remains") should contain theSameElementsAs Seq("value 5")

      val replaced2 = headers.replace(
        "test-replaced-1" -> "value 7",
        "test-replaced-2" -> "value 8",
        "test-replaced-2" -> "value 9",
      )
      replaced2.getAll("test-replaced-1") should contain theSameElementsAs Seq("value 7")
      replaced2.getAll("test-replaced-2") should contain theSameElementsAs Seq("value 8", "value 9")
      replaced2.getAll("test-remains") should contain theSameElementsAs Seq("value 5")

      val replaced3 = headers.replace(Headers(
        "test-replaced-1" -> "value 10",
        "test-replaced-2" -> "value 11",
        "test-replaced-2" -> "value 12",
      ))
      replaced3.getAll("test-replaced-1") should contain theSameElementsAs Seq("value 10")
      replaced3.getAll("test-replaced-2") should contain theSameElementsAs Seq("value 11", "value 12")
      replaced3.getAll("test-remains") should contain theSameElementsAs Seq("value 5")
    }

    "add one or more headers" in {
      val headers = Headers(
        "test-existing-1" -> "value 1",
        "test-existing-2" -> "value 2",
      )

      val added1 = headers.add("test-new-1", "value 3")
      added1.getAll("test-existing-1") should contain theSameElementsAs Seq("value 1")
      added1.getAll("test-existing-2") should contain theSameElementsAs Seq("value 2")
      added1.getAll("test-new-1") should contain theSameElementsAs Seq("value 3")

      val added2 = headers.add("test-existing-1", "value 4")
      added2.getAll("test-existing-1") should contain theSameElementsAs Seq("value 1", "value 4")
      added2.getAll("test-existing-2") should contain theSameElementsAs Seq("value 2")

      val added3 = headers.add(
        "test-existing-1" -> "value 5",
        "test-new-1" -> "value 6",
        "test-new-1" -> "value 7",
      )
      added3.getAll("test-existing-1") should contain theSameElementsAs Seq("value 1", "value 5")
      added3.getAll("test-existing-2") should contain theSameElementsAs Seq("value 2")
      added3.getAll("test-new-1") should contain theSameElementsAs Seq("value 6", "value 7")

      val added4 = headers.add(Headers(
        "test-existing-1" -> "value 8",
        "test-new-1" -> "value 9",
        "test-new-1" -> "value 10",
      ))
      added4.getAll("test-existing-1") should contain theSameElementsAs Seq("value 1", "value 8")
      added4.getAll("test-existing-2") should contain theSameElementsAs Seq("value 2")
      added4.getAll("test-new-1") should contain theSameElementsAs Seq("value 9", "value 10")
    }
  }

  "The header parser" should {
    "pick up multiple instances of a header" in {
      val parsed = Headers.parse(Source.fromString(
        """test-parsed-1: value 1
          |test-parsed-1: value 2
        """.stripMargin
      ))

      parsed.getAll("test-parsed-1") should contain theSameElementsAs Seq("value 1", "value 2")
    }

    "skip non-header lines" in {
      val parsed = Headers.parse(Source.fromString(
        """test-parsed-2: value 1
          | !!! not a header line !!!
          |test-parsed-3: value 2
        """.stripMargin
      ))

      parsed.iterator.length should equal (2)
    }

    "stop reading headers at a blank line" in {
      val parsed = Headers.parse(Source.fromString(
        """test-parsed-4: value 1
          |test-parsed-5: value 2
          |test-parsed-6: value 3
          |
          |test-parsed-7: value 4
        """.stripMargin
      ))

      parsed.iterator.length should equal (3)
      parsed.get("test-parsed-7") should equal (None)
    }
  }

  "The header renderer" should {
    "output multiple values when a header has multiple" in {
      val rendered = Headers(
        "test-render-1" -> "value 1",
        "test-render-1" -> "value 2"
      ).render

      rendered.linesIterator.length should equal (2)
    }

    "capitalize the first letter of each header segment" in {
      val rendered = Headers("tEsT-rEnDeR-1" -> "value 1").render
      rendered should equal ("Test-Render-1: value 1")
    }
  }
}