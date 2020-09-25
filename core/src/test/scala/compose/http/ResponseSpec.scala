package compose.http

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import compose.rendering.Renderer

class ResponseSpec extends compose.Spec {
  "The response writer" should {
    "write out a response, with body" in {
      val resp = Response(
        Version.HTTP_0_9,
        Status.OK,
        Headers("header-one" -> "value-one"),
        new ByteArrayInputStream("body body".getBytes(Headers.encoding)),
      )
      val output = new ByteArrayOutputStream()

      resp.writeTo(output)
      output.toString(Headers.encoding) should equal(
        "HTTP/0.9 200 OK\r\nHeader-One: value-one\r\n\r\nbody body"
      )
    }

    "encode the header section correctly" in {
      val testString = "ëñçødîng tèßt"
      val resp = Response(
        Version.HTTP_0_9,
        Status.OK,
        Headers("test-string" -> testString),
        new ByteArrayInputStream(Array[Byte]()),
      )
      val output = new ByteArrayOutputStream()

      resp.writeTo(output)
      output.toString(Headers.encoding) should equal(
        s"HTTP/0.9 200 OK\r\nTest-String: $testString\r\n\r\n"
      )
    }
  }

  "The response builder" when {
    case class BuilderTestCase(good: Boolean)
    val testCaseRenderer = Renderer[BuilderTestCase](tc =>
      if (tc.good)
        Renderer.Success(
          Headers("test-header-1" -> "default value 1", "test-header-2" -> "default value 2"),
          new ByteArrayInputStream(Array[Byte]()),
        )
      else
        Renderer.Failure("Testing rendering failure")
    )

    "rendering succeeds" should {
      "use the provided status" in {
        val resp = Response[BuilderTestCase](
          BuilderTestCase(good = true),
          status = Status.ImATeapot,
        )(testCaseRenderer)

        resp.status should equal(Status.ImATeapot)
      }

      "pass on the default headers" in {
        val resp = Response[BuilderTestCase](BuilderTestCase(good = true))(testCaseRenderer)

        resp.headers.get("test-header-1") should equal(Some("default value 1"))
        resp.headers.get("test-header-2") should equal(Some("default value 2"))
      }

      "allow the caller to override default headers" in {
        val resp = Response[BuilderTestCase](
          BuilderTestCase(good = true),
          headers =
            Headers("test-header-1" -> "overridden value 1", "test-header-3" -> "overridden value 3"),
        )(testCaseRenderer)

        resp.headers.get("test-header-1") should equal(Some("overridden value 1"))
        resp.headers.get("test-header-2") should equal(Some("default value 2"))
        resp.headers.get("test-header-3") should equal(Some("overridden value 3"))
      }
    }

    "rendering fails" should {
      "replace the provided status with a 500" in {
        val resp = Response[BuilderTestCase](
          BuilderTestCase(good = false),
          status = Status.ImATeapot,
        )(testCaseRenderer)

        resp.status should equal(Status.InternalServerError)
      }
    }
  }

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
