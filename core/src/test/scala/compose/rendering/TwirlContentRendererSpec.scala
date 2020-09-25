package compose.rendering

import play.twirl.api._

class TwirlContentRendererSpec extends compose.Spec {
  val baseString = "hello twirl test"
  val content: Content = Html(baseString)

  "The twirl renderer" should {
    "render the content into a string" in {
      val result = new TwirlContentRenderer()(implicits.stringRenderer).render(content)

      result shouldBe a[Renderer.Success]
      val bodyBytes = result.asInstanceOf[Renderer.Success].bodyStream.readAllBytes()
      (new String(bodyBytes, "UTF-8")) should equal(baseString)
    }

    "overwrite content-type header" in {
      val result = new TwirlContentRenderer()(implicits.stringRenderer).render(content)

      result shouldBe a[Renderer.Success]
      val headers = result.asInstanceOf[Renderer.Success].defaultHeaders

      headers.get("content-type").value should startWith(content.contentType)
    }

    "extract the encoding if possible" in {
      val result = new TwirlContentRenderer()(new EncodedStringRenderer("UTF-16")).render(content)

      result shouldBe a[Renderer.Success]
      val headers = result.asInstanceOf[Renderer.Success].defaultHeaders

      headers.get("content-type").value should endWith(s"""; charset="UTF-16"""")
    }
  }
}
