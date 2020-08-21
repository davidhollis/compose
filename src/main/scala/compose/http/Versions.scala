package compose.http

import scala.util.Try

object Versions { // TODO this should be an enum, not a float. Arbitrary versions are not allowed
  val HTTP_0_9: Float = 0.9f
  val HTTP_1_0: Float = 1.0f
  val HTTP_1_1: Float = 1.1f
  val HTTP_2_0: Float = 2.0f

  lazy val all: Set[Float] = Set(HTTP_0_9, HTTP_1_0, HTTP_1_1, HTTP_2_0)
}

object Version {
  private val versionDeclaration = """HTTP/([0-9.]+)""".r

  def unapply(versionStr: String): Option[Float] = versionStr match {
    case versionDeclaration(versionNumber) => {
      Try[Float](versionNumber.toFloat).toOption.flatMap { version =>
        if (Versions.all.contains(version))
          Some(version)
        else
          None
      }
    }
    case _ => None
  }
}
