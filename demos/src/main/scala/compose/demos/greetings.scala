package compose.demos

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import net.ceedubs.ficus.Ficus._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

import compose.Application
import compose.http.{Request, Response, Method, Status, Headers, Version}
import compose.rendering.implicits._
import compose.server.SimpleDevelopmentServer

class GreetingsApplication(greeting: String) extends Application[AnyRef] {
  import Version.HTTP_1_1

  private val greetingPath = """^/greet/(.*)$""".r

  def apply(request: Request[AnyRef]): Future[Response] = request match {
    case Request(_, Method.Get, greetingPath(name), _, _) => {
      val greetingBody = s"${greeting}, ${name}!\n"
      Future.successful(Response[String](greetingBody))
    }
    case Request(_, _, path, _, _) => {
      val errorBody = s"No document found at $path\n"
      Future.successful(Response[String](
        errorBody,
        status = Status.NotFound,
      ))
    }
  }
}

object GreetingsDemo extends scala.App with StrictLogging {
  val server = SimpleDevelopmentServer(
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

  server.boot { config => implicit executionContext =>
    val greeting = config.as[Option[String]]("application.greeting").getOrElse("Greetings")
    logger.info(s"Loaded greeting: ${greeting}")
    new GreetingsApplication(greeting)
  }
}