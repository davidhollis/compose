package compose.rendering

import java.io.ByteArrayInputStream

import compose.http.Headers

class RendererSpec extends compose.Spec {
  "Renderers" should {
    "compose" in {
      case class RenderCompositionTestCaseA(valueA: Int)
      case class RenderCompositionTestCaseB(valueB: Int)

      val testCaseARenderer = Renderer[RenderCompositionTestCaseA] { rctca =>
        Renderer.Success(
          Headers("test-value" -> rctca.valueA.toString()),
          new ByteArrayInputStream(Array[Byte]()),
        )
      }
      val testCaseBRenderer = testCaseARenderer.compose[RenderCompositionTestCaseB] { rctcb =>
        RenderCompositionTestCaseA(rctcb.valueB * 2)
      }

      val result = testCaseBRenderer.render(RenderCompositionTestCaseB(5))
      result shouldBe a[Renderer.Success]
      val headers = result.asInstanceOf[Renderer.Success].defaultHeaders
      headers.get("test-value").value should equal("10")
    }
  }
}
