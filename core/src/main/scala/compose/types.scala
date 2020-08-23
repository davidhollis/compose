package compose

import com.typesafe.config.{Config, ConfigFactory}
import java.io.InputStream
import scala.concurrent.{ExecutionContext, Future}

import compose.http.{Request, Response}

// Principle: A web application is a function Request => Future[Response].
trait Application[-B] extends (Request[B] => Future[Response])

// Principle: A web server is a function that takes in an application and does not return a value.
trait Server extends (Application[InputStream] => Unit) {
  // Principle: A web server has a configuration.
  val config: Config

  // Principle: A web server creates an execution context.
  implicit val executionContext: ExecutionContext

  // Principle: A setup function takes the server's configuration and execution context and uses them to build an application.
  // Principle: A server's boot method calls the setup function to build the application, then passes that application to the server function.
  def boot(
    setupApplication: Config => ExecutionContext => Application[InputStream]
  ): Unit = {
    val application = setupApplication(config)(executionContext)
    this(application)
  }
}
