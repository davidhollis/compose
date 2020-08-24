package compose.server

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import java.io.InputStream
import java.net.{InetAddress, ServerSocket}
import java.util.concurrent.{Executors, ExecutorService}
import net.ceedubs.ficus.Ficus._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import compose.{Application, Server}
import compose.http.{Request, Response, Status}
import compose.rendering.implicits._

case class SimpleDevelopmentServer(config: Config) extends Server with StrictLogging {
  private val serverConfig: Config = config.getAs[Config]("compose.server").getOrElse(ConfigFactory.empty())
  implicit lazy val executionContext: ExecutionContext = {
    val threadPool: ExecutorService =
      serverConfig.getAs[Int]("numThreads")
        .map(Executors.newFixedThreadPool(_))
        .getOrElse(Executors.newSingleThreadExecutor())
    ExecutionContext.fromExecutorService(threadPool)
  }
  def apply(application: Application[InputStream]): Unit = {
    val socket = new ServerSocket(
      serverConfig.getAs[Int]("port").getOrElse(SimpleDevelopmentServer.Defaults.portNumber),
      serverConfig.getAs[Int]("backlog").getOrElse(SimpleDevelopmentServer.Defaults.backlog),
      InetAddress.getByName(
        serverConfig.getAs[String]("host").getOrElse(SimpleDevelopmentServer.Defaults.host)
      ),
    )
    logger.info(s"Listening on ${socket.getInetAddress()}:${socket.getLocalPort()}")

    while (true) {
      val connection = socket.accept()
      Future[Option[Request[InputStream]]] {
        logger.debug("Received request. Validating.")
        Request.parse(connection.getInputStream())
      }.flatMap {
        case Some(request) => {
          logger.info(s"Received ${request.method} ${request.target.toString}")
          application(request)
        }
        case None => {
          logger.info("Received bad request.")
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
              logger.error(s"Uncaught exception. Sending response with status ${Status.InternalServerError.code}", err)
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
  object Defaults {
    val portNumber: Int = 8090
    val backlog: Int = 16
    val host: String = "127.0.0.1"
  }

  def internalServerErrorResponse(err: Throwable): Response = {
    val errBody = serializeError(err)
    Response[String](
      errBody,
      status = Status.InternalServerError,
    )
  }

  def serializeError(err: Throwable): String = {
    s"""
      |An error occurred while processing this request.
      |(If you're seeing this error in a production environment, please switch to the production server.)
      |
      |${renderThrowable(err)}
    """.stripMargin
  }

  def renderThrowable(err: Throwable, root: Boolean = true): String = {
    val prefix = if (!root) "Caused by: " else ""
    val base = s"${prefix}${err.toString()}\n"
    val trace = (for {
      line <- err.getStackTrace()
    } yield s"    at ${line.toString()}\n").mkString
    val causes =
      Option[Throwable](err.getCause())
        .map(renderThrowable(_, root=false))
        .getOrElse("")
    base + trace + causes
  }

  val badRequestError: String = "Failed to parse HTTP request.\n"

  lazy val badRequestResponse: Future[Response] = Future.successful(
    Response[String](
      SimpleDevelopmentServer.badRequestError,
      status = Status.BadRequest,
    )
  )
}
