package compose.demos

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import net.ceedubs.ficus.Ficus._
import scala.concurrent.Future

import compose.Application
import compose.http.{ Method, Request, RequestTarget, Response, Status }
import compose.rendering.implicits._
import compose.server.SimpleDevelopmentServer

class GreetingsApplication(greeting: String) extends Application[AnyRef] {
  private val greetingPath = """^/greet/(.*)$""".r

  def apply(request: Request[AnyRef]): Future[Response] =
    request match {
      case Request(_, Method.Get, RequestTarget.Path(greetingPath(name), _), _, _) => {
        val greetingBody = s"${greeting}, ${name}!\n"
        Future.successful(Response[String](greetingBody))
      }
      case Request(_, _, path, _, _) => {
        val errorBody = s"No document found at $path\n"
        Future.successful(
          Response[String](
            errorBody,
            status = Status.NotFound,
          )
        )
      }
    }

}

object GreetingsDemo extends scala.App with StrictLogging {

  val server: SimpleDevelopmentServer = SimpleDevelopmentServer(
    ConfigFactory.parseString("""
      compose.server {
        host = "127.0.0.1"
        port = 8080
        numThreads = 5
      }
      application {
        greeting = "Hello"
      }
    """)
  )

  server.boot { config => _ =>
    val greeting = config.as[Option[String]]("application.greeting").getOrElse("Greetings")
    logger.info(s"Loaded greeting: ${greeting}")
    new GreetingsApplication(greeting)
  }
}
