package compose.server

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import java.io.InputStream
import java.net.{InetAddress, ServerSocket}
import java.util.concurrent.{Executors, ExecutorService}
import net.ceedubs.ficus.Ficus._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Success, Failure}

import compose.{Application, Server}
import compose.http.{Request, Response, Headers, Version}

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
      serverConfig.getAs[Int]("port").getOrElse(8090),
      serverConfig.getAs[Int]("backlog").getOrElse(16),
      InetAddress.getByName(
        serverConfig.getAs[String]("host").getOrElse("127.0.0.1")
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
          logger.info(s"Received ${request.method} ${request.path}")
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
              logger.error(s"Uncaught exception. Sending response with status ${Response.Status.InternalServerError.code}", err)
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
  def internalServerErrorResponse(err: Throwable): Response = {
    val errBody = serializeError(err)
    Response.withStringBody(
      Version.HTTP_1_1,
      Response.Status.InternalServerError,
      Headers(
        "Content-Type" -> "text/plain",
        "Content-Length" -> errBody.length.toString(),
      ),
      errBody,
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
        .map(renderThrowable(_, false))
        .getOrElse("")
    base + trace + causes
  }

  val badRequestError: String = "Failed to parse HTTP request.\n"

  lazy val badRequestResponse: Future[Response] = Future.successful(
    Response.withStringBody(
      Version.HTTP_1_1,
      Response.Status.BadRequest,
      Headers(
        "Content-Type" -> "text/plain",
        "Content-Length" -> badRequestError.length.toString(),
      ),
      SimpleDevelopmentServer.badRequestError,
    )
  )
}