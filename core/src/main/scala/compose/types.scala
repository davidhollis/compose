package compose

import com.typesafe.config.Config
import java.io.InputStream
import scala.concurrent.{ ExecutionContext, Future }

import compose.http.{ Request, Response }

/** A web application is a function mapping an HTTP request to an eventual HTTP response.
  *
  * `Application` is one of the fundamental types in compose. Generally, to build an application on
  * the compose framework, the author will assemble an `Application[InputStream]` by composing
  * other types of `Application` with combinators or with other functions that transform requests
  * and responses.
  *
  * @tparam Body
  *   the type of the request body
  */
trait Application[-Body] extends (Request[Body] => Future[Response])

/** A web server is a procedure that takes in a web application.
  *
  * A ''useful'' web server is one that reads HTTP requests from some source (generally a socket of
  * some kind), uses its application to produce HTTP responses, and then writes those responses out
  * somewhere.
  *
  * The server will end up creating requests with [[java.io.InputStream]]s as bodies, so the
  * application that it serves should have type `Application[InputStream]`. That application may
  * (and often in practice will) transform the request and delegate to other types of applications.
  */
trait Server extends (Application[InputStream] => Unit) {

  /** The application configuration */
  val config: Config

  /** The execution context the server provides for its application */
  implicit val executionContext: ExecutionContext

  /** The server process entry point.
    *
    * `boot` calls `setupApplication` with the application config and the execution context, then
    * calls the server function on the resulting application.
    *
    * @param setupApplication
    *   a function which uses the configuration and execution context to build the application that
    *   this server will serve
    */
  def boot(
    setupApplication: Config => ExecutionContext => Application[InputStream]
  ): Unit = {
    val application = setupApplication(config)(executionContext)
    this(application)
  }

}
