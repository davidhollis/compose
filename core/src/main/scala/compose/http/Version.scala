package compose.http

import scala.util.Try

sealed abstract class Version(val number: Float) {
  override lazy val toString: String = s"HTTP/$number"
}

object Version {
  object HTTP_0_9 extends Version(0.9f)
  object HTTP_1_0 extends Version(1.0f)
  object HTTP_1_1 extends Version(1.1f)
  object HTTP_2_0 extends Version(2.0f)

  lazy val byNumber: Map[Float, Version] = Map(
    (for { v <- Seq(HTTP_0_9, HTTP_1_0, HTTP_1_1, HTTP_2_0) } yield (v.number -> v)): _*
  )

  private val versionRegex = """HTTP/([0-9.]+)""".r

  def unapply(versionStr: String): Option[Version] =
    versionStr match {
      case versionRegex(versionNumber) => {
        Try[Float](versionNumber.toFloat).toOption.flatMap(byNumber.get)
      }
      case _ => None
    }

}
