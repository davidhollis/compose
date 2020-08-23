package compose.http

class VersionSpec extends compose.Spec {
  "The version extractor" should {
    "produce the correct versions from HTTP version strings" in {
      val cases = Seq(
        ("HTTP/0.9", Version.HTTP_0_9),
        ("HTTP/1.0", Version.HTTP_1_0),
        ("HTTP/1.1", Version.HTTP_1_1),
        ("HTTP/2.0", Version.HTTP_2_0),
      )

      for ((versionString, expectedVersion) <- cases) {
        Version.unapply(versionString) should equal (Some(expectedVersion))
      }
    }

    "produce nothing from invalid version strings" in {
      Version.unapply("HTTP/0.1") should equal (None)
    }
  }
}