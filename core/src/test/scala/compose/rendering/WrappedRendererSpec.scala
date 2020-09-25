package compose.rendering

import java.io.ByteArrayInputStream

import compose.http.Headers

class WrappedRendererSpec extends compose.Spec {
  case class WrappedRendererTestCaseA(valueA: Byte)
  case class WrappedRendererTestCaseB(valueB: Byte)

  object CaseBRenderer extends Renderer[WrappedRendererTestCaseB] {

    def render(body: WrappedRendererTestCaseB): Renderer.Result =
      if (body.valueB >= 0)
        Renderer.Success(
          Headers(
            "test-header-b-1" -> "removed",
            "test-header-b-2" -> "preserved",
          ),
          new ByteArrayInputStream(Array[Byte](body.valueB)),
        )
      else
        Renderer.Failure(s"${body.valueB} < 0")

  }

  object CaseARenderer
      extends WrappedRenderer[WrappedRendererTestCaseA, WrappedRendererTestCaseB](CaseBRenderer) {

    def transformBody(body: WrappedRendererTestCaseA): WrappedRendererTestCaseB =
      WrappedRendererTestCaseB((body.valueA * 3).toByte)

    override def transformHeaders(
      body: WrappedRendererTestCaseA,
      initialHeaders: Headers,
    ): Headers =
      initialHeaders.replace(
        "test-header-a-1" -> "introduced",
        "test-header-b-1" -> "overwritten",
      )

  }

  "A wrapped renderer" should {
    "transform the response body into a form the base renderer accepts" in {
      val result = CaseARenderer.render(WrappedRendererTestCaseA(7))

      result shouldBe a[Renderer.Success]
      val body = result.asInstanceOf[Renderer.Success].bodyStream.readAllBytes()
      body(0) should equal(21)
    }

    "be able to transform the initial headers" in {
      val result = CaseARenderer.render(WrappedRendererTestCaseA(19))

      result shouldBe a[Renderer.Success]
      val headers = result.asInstanceOf[Renderer.Success].defaultHeaders
      headers.get("test-header-a-1") should equal(Some("introduced"))
      headers.get("test-header-b-1") should equal(Some("overwritten"))
      headers.get("test-header-b-2") should equal(Some("preserved"))
    }

    "pass on failures from the base renderer" in {
      val result = CaseARenderer.render(WrappedRendererTestCaseA(-6))

      result shouldBe a[Renderer.Failure]
    }
  }
}
