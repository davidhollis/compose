package compose.server

import com.typesafe.config.{ Config, ConfigFactory }
import com.typesafe.scalalogging.StrictLogging
import java.io.InputStream
import java.net.{ InetAddress, ServerSocket }
import java.util.concurrent.{ ExecutorService, Executors }
import net.ceedubs.ficus.Ficus._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import compose.{ Application, Server }
import compose.http.{ Request, Response, Status }
import compose.http.attributes._
import compose.rendering.implicits._

/** A basic server implementation suitable for local development.
  *
  * '''Configuration'''
  *
  * The following options are supported:
  *
  *   - `numThreads`: the number of threads to spawn to respond to requests ''(default: 1)''
  *   - `host`: the interface to listen on ''(default: 127.0.0.1)''
  *   - `port`: the port to bind to ''(default: 8090)''
  *   - `backlog`: the max connection backlog ''(default: 16)''
  *
  * @param config
  *   the server and application configuration. The server config is expected to be under
  *   `compose.server`.
  */
case class SimpleDevelopmentServer(config: Config) extends Server with StrictLogging {

  private val serverConfig: Config =
    config.getAs[Config]("compose.server").getOrElse(ConfigFactory.empty())

  implicit lazy val executionContext: ExecutionContext = {
    val threadPool: ExecutorService =
      serverConfig
        .getAs[Int]("numThreads")
        .map(Executors.newFixedThreadPool(_))
        .getOrElse(Executors.newSingleThreadExecutor())
    ExecutionContext.fromExecutorService(threadPool)
  }

  @SuppressWarnings(
    Array(
      // We're intentionally looping forver, not iterating over a collection, so while is actually appropriate here
      "scalafix:DisableSyntax.while"
    )
  )
  def apply(application: Application[InputStream, NoAttrs]): Unit = {
    val socket = new ServerSocket(
      serverConfig.getAs[Int]("port").getOrElse(8090),
      serverConfig.getAs[Int]("backlog").getOrElse(16),
      InetAddress.getByName(
        serverConfig.getAs[String]("host").getOrElse("127.0.0.1")
      ),
    )
    logger.info(s"Listening on ${socket.getInetAddress()}:${socket.getLocalPort()}")

    while (true) {
      val connection = socket.accept()
      Future[Option[Request[InputStream, NoAttrs]]] {
        logger.debug("Received request. Validating.")
        Request.parse(connection.getInputStream())
      }.flatMap {
        case Some(request) => {
          logger.info(s"Received ${request.method} ${request.target.toString}")
          application(request)
        }
        case None => {
          logger.info(s"Received bad request.")
          SimpleDevelopmentServer.badRequestResponse
        }
      }.onComplete { reponseOrError =>
        val outputStream = connection.getOutputStream()
        try {
          reponseOrError match {
            case Success(response) => {
              logger.info(s"Sending response with status ${response.status.code}")
              response.writeTo(outputStream)
            }
            case Failure(err) => {
              logger.error(
                s"Uncaught exception. Sending response with status ${Status.InternalServerError.code}",
                err,
              )
              SimpleDevelopmentServer.internalServerErrorResponse(err).writeTo(outputStream)
            }
          }
        } finally {
          outputStream.close()
          connection.close()
        }
      }
    }
  }

}

object SimpleDevelopmentServer {

  private def internalServerErrorResponse(err: Throwable): Response = {
    val errBody = serializeError(err)
    Response[String](
      errBody,
      status = Status.InternalServerError,
    )
  }

  private def serializeError(err: Throwable): String = {
    s"""
       |An error occurred while processing this request.
       |(If you're seeing this error in a production environment, please switch to the production server.)
       |
       |${renderThrowable(err)}
    """.stripMargin
  }

  private def renderThrowable(err: Throwable, root: Boolean = true): String = {
    val prefix = if (!root) "Caused by: " else ""
    val base = s"${prefix}${err.toString()}\n"
    val trace = (for {
      line <- err.getStackTrace()
    } yield s"    at ${line.toString()}\n").mkString
    val causes =
      Option[Throwable](err.getCause())
        .map(renderThrowable(_, false))
        .getOrElse("")
    base + trace + causes
  }

  private val badRequestError: String = "Failed to parse HTTP request.\n"

  private lazy val badRequestResponse: Future[Response] = Future.successful(
    Response[String](
      SimpleDevelopmentServer.badRequestError,
      status = Status.BadRequest,
    )
  )

}
