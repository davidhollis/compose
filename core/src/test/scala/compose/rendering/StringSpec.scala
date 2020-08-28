package compose.rendering

import compose.http.Response.Renderer

class StringSpec extends compose.Spec {
  "The string renderer" should {
    "encode the body properly" in {
      val testString = "test string rendering"
      val result = implicits.encodedStringRenderer("UTF-16").render(testString)

      result shouldBe a[Renderer.Success]
      val encodedBytes = result.asInstanceOf[Renderer.Success].bodyStream.readAllBytes()
      (new String(encodedBytes, "UTF-16")) should equal(testString)
      (new String(encodedBytes, "UTF-8")) should not equal (testString)
    }

    "handle invalid encoding names" in {
      val testString = "test failed string rendering"
      val result = implicits.encodedStringRenderer("not a real encoding").render(testString)

      result shouldBe a[Renderer.Failure]
    }

    "insert a charset tag into the content-type header" in {
      val testString = "test charset rendering"
      val result = implicits.encodedStringRenderer("UTF-16").render(testString)

      result shouldBe a[Renderer.Success]
      val headers = result.asInstanceOf[Renderer.Success].defaultHeaders
      headers.get("content-type").value should equal("""text/plain; charset="UTF-16"""")
    }

    "report the content length properly" in {
      val testString = "test content-length"
      val result = implicits.encodedStringRenderer("UTF-16").render(testString)

      result shouldBe a[Renderer.Success]
      val headers = result.asInstanceOf[Renderer.Success].defaultHeaders
      val encodedBytes = result.asInstanceOf[Renderer.Success].bodyStream.readAllBytes()
      headers.get("content-length").value should equal(encodedBytes.length.toString())
      headers.get("content-length").value should not equal (testString.length.toString())
    }
  }
}
