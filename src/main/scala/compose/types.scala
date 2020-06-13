package compose

import scala.concurrent.Future

import compose.http.{Request, Response}

// Principle: A web application is a function Request => Future[Response]
trait Application extends (Request => Future[Response])
