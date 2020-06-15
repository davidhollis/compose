package compose.server

import com.typesafe.config.{Config, ConfigFactory}
import java.net.{InetAddress, ServerSocket}
import java.util.concurrent.{Executors, ExecutorService}
import net.ceedubs.ficus.Ficus._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Success, Failure}

import compose.{Application, Server}
import compose.http.{Request, Response, Headers, Versions}

case class SimpleDevelopmentServer(config: Config) extends Server {
  private val serverConfig: Config = config.getAs[Config]("compose.server").getOrElse(ConfigFactory.empty())
  implicit lazy val executionContext: ExecutionContext = {
    val threadPool: ExecutorService =
      serverConfig.getAs[Int]("numThreads")
        .map(Executors.newFixedThreadPool(_))
        .getOrElse(Executors.newSingleThreadExecutor())
    ExecutionContext.fromExecutorService(threadPool)
  }
  def apply(application: Application): Unit = {
    val socket = new ServerSocket(
      serverConfig.getAs[Int]("port").getOrElse(8090),
      serverConfig.getAs[Int]("backlog").getOrElse(16),
      InetAddress.getByName(
        serverConfig.getAs[String]("host").getOrElse("127.0.0.1")
      ),
    )

    while (true) {
      val connection = socket.accept()
      Future[Option[Request]] {
        Request.parse(Source.fromInputStream(connection.getInputStream()))
      }.flatMap {
        case Some(request) => application(request)
        case None => Future.successful(Response(
            Versions.HTTP_1_1,
            Response.Status.BadRequest,
            Headers(
              "Content-Type" -> "text/plain",
            ),
            Source.fromString(SimpleDevelopmentServer.badRequestError),
        ))
      }.onComplete {
        case Success(response) => response.writeTo(connection.getOutputStream())
        case Failure(err) => {
          Response(
            Versions.HTTP_1_1,
            Response.Status.InternalServerError,
            Headers(
              "Content-Type" -> "text/plain",
            ),
            Source.fromString(SimpleDevelopmentServer.serializeError(err)),
          ).writeTo(connection.getOutputStream())
        }
      }
    }
  }
}

object SimpleDevelopmentServer {
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

  val badRequestError: String = "Failed to parse HTTP request."
}