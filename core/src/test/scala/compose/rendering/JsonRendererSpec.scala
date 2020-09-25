package compose.rendering

import play.api.libs.json._

import compose.http.Response.Renderer

class JsonRendererSpec extends compose.Spec {

  "The JSON renderer" should {
    object RendererTest {
      val testOutput: JsValue = Json.obj("testing" -> 123)
      lazy val testOutputString = testOutput.toString()
      implicit val writes: Writes[RendererTest.type] = Writes[RendererTest.type] { _ => testOutput }
    }

    "use a Writes if one is available" in {
      val result = new JsonRenderer[RendererTest.type].render(RendererTest)

      result shouldBe a[Renderer.Success]
      val encodedBytes = result.asInstanceOf[Renderer.Success].bodyStream.readAllBytes()
      (new String(encodedBytes, "UTF-8")) should equal(RendererTest.testOutputString)
    }

    "set the content-type appropriately" in {
      val result = new JsonRenderer[RendererTest.type].render(RendererTest)

      result shouldBe a[Renderer.Success]
      val headers = result.asInstanceOf[Renderer.Success].defaultHeaders
      headers.get("content-type").value should equal("""application/json; charset="UTF-8"""")
    }

    "set the content-length appropriately" in {
      val result = new JsonRenderer[RendererTest.type].render(RendererTest)

      result shouldBe a[Renderer.Success]
      val headers = result.asInstanceOf[Renderer.Success].defaultHeaders
      val encodedBytes = result.asInstanceOf[Renderer.Success].bodyStream.readAllBytes()
      headers.get("content-length").value should equal(encodedBytes.length.toString())
    }
  }

  "The implicit JSON renderer" should {
    "be available wherever there's a Writes" in {
      """
        |import compose.rendering.implicits.json._
        |import play.api.libs.json._
        |case class Foo(f: Int)
        |implicit def writes = Writes[Foo]{ _ => JsNull }
        |val verify = implicitly[Renderer[Foo]]
      """.stripMargin should compile
    }

    "not be available where there's no Writes" in {
      """
        |import compose.rendering.implicits.json._
        |import play.api.libs.json._
        |case class Bar(b: Int)
        |val falsify = implicitly[Renderer[Bar]]
      """.stripMargin shouldNot compile
    }
  }
}
