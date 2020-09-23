package compose.http

import java.net.URI

class RequestTargetSpec extends compose.Spec {

  // format: off
  val authorityExamples = Seq(
    // authority string                                                  host               user              password          port
    "foo.example.com"                         -> RequestTarget.Authority("foo.example.com", None,             None,             None),
    "foo.example.com:12345"                   -> RequestTarget.Authority("foo.example.com", None,             None,             Some(12345)),
    "testuser@foo.example.com"                -> RequestTarget.Authority("foo.example.com", Some("testuser"), None,             None),
    "testuser@foo.example.com:12345"          -> RequestTarget.Authority("foo.example.com", Some("testuser"), None,             Some(12345)),
    "testuser:testpass@foo.example.com"       -> RequestTarget.Authority("foo.example.com", Some("testuser"), Some("testpass"), None),
    "testuser:testpass@foo.example.com:12345" -> RequestTarget.Authority("foo.example.com", Some("testuser"), Some("testpass"), Some(12345)),
  )
  // format: on

  "The request target parser" should {
    "recognize asterisk targets" in {
      RequestTarget.parse("*") should equal(RequestTarget.Asterisk)
    }

    "recognize absolute targets" in {
      RequestTarget
        .parse("https://absolute.example.com/path?query#fragment") shouldBe a[RequestTarget.Absolute]
    }

    "recognize authority targets" in {
      for ((host, expected) <- authorityExamples) {
        RequestTarget.parse(host) should equal(expected)
      }
    }

    "recognize origin targets with query strings" in {
      val parsed = RequestTarget.parse("/some/absolute/path?param1=value1&param2=value2")

      parsed shouldBe a[RequestTarget.Path]
      val parsedPath = parsed.asInstanceOf[RequestTarget.Path]
      parsedPath.path should equal("/some/absolute/path")
      parsedPath.queryParams should not be (empty)
    }

    "turn nonsense into an origin target" in {
      val nonsense = "cnasoichdsoucelsck?ncocpoewcd%^&*()"
      val parsed = RequestTarget.parse(nonsense)

      parsed shouldBe a[RequestTarget.Path]
      val parsedPath = parsed.asInstanceOf[RequestTarget.Path]
      parsedPath.path should equal(nonsense)
      parsedPath.queryParams should be(empty)
    }
  }

  "The Asterisk target" should {
    "have a string representation" in {
      RequestTarget.Asterisk.toString should equal("*")
    }
  }

  "Absolute targets" should {
    "have a string representation" in {
      val stringRepr =
        RequestTarget.Absolute(new URI("https://absolute.example.com/path?query#fragment")).toString
      noException should be thrownBy { new URI(stringRepr) }
    }
  }

  "Authority targets" should {
    "have a string representation" in {
      for ((expected, authTarget) <- authorityExamples) {
        authTarget.toString should equal(expected)
      }
    }
  }

  "Origin targets" which {
    "are well-formed" should {
      "have string representations" in {
        val stringRepr =
          RequestTarget.Path("/some/path", Map("a" -> Seq("a1", "a2"), "b" -> Seq("b1"))).toString

        stringRepr should startWith("/some/path?")
        stringRepr should include("a=a1")
        stringRepr should include("a=a2")
        stringRepr should include("b=b1")
      }
    }

    "are nonsense" should {
      "have string representations" in {
        val stringRepr = RequestTarget.Path("garbage?&=/", Map.empty).toString
        stringRepr should equal("garbage%3F%26%3D/")
      }
    }
  }
}
