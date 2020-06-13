package compose

import com.typesafe.config.{Config, ConfigFactory}
import scala.concurrent.Future

import compose.http.{Request, Response}

// Principle: A web application is a function Request => Future[Response]
trait Application extends (Request => Future[Response])

// Principle: A web server is a function that takes in a config and an application, and which does not return
trait Server extends ((Config, Application) => Nothing) {
  def boot(
    config: => Config
  )(
    setupApplication: Config => Application
  ): Nothing = {
    val application = setupApplication(config)
    this(config, application)
  }

  def boot(setupApplication: Config => Application): Nothing =
    this.boot(ConfigFactory.load())(setupApplication)
}
