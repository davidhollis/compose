package compose.http

import java.io.ByteArrayInputStream

class RequestSpec extends compose.Spec {
  "The request parser" should {
    "parse an HTTP request" in  {
      val requestText = Seq(
        "POST /test/path?with=params HTTP/1.1",
        "User-Agent: scalatest",
        "Accept: */*",
        "Content-Type: text/plain",
        "",
        "body body",
        "body body",
      ).mkString("\r\n")
      val requestStream = new ByteArrayInputStream(requestText.getBytes("ISO-8859-1"))
      val parsedRequest = Request.parse(requestStream)

      parsedRequest.value.version should equal (Version.HTTP_1_1)
      parsedRequest.value.method should equal (Method.Post)
      parsedRequest.value.path should equal ("/test/path?with=params")

      parsedRequest.value.headers.iterator.length should equal (3)
      parsedRequest.value.headers.getAll("User-Agent") should contain theSameElementsAs Seq("scalatest")
      parsedRequest.value.headers.getAll("Accept") should contain theSameElementsAs Seq("*/*")
      parsedRequest.value.headers.getAll("Content-Type") should contain theSameElementsAs Seq("text/plain")

      val bodyString = new String(parsedRequest.value.body.readAllBytes(), "ISO-8859-1")
      bodyString should equal ("body body\r\nbody body")
    }
  }
}