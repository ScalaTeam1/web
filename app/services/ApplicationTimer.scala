package services

import play.api.Logger
import play.api.inject.ApplicationLifecycle

import java.time.{Clock, Instant}
import javax.inject._
import scala.concurrent.Future

@Singleton
class ApplicationTimer @Inject()(clock: Clock, appLifecycle: ApplicationLifecycle) {

  private val logger: Logger = Logger(this.getClass)

  // This code is called when the application starts.
  private val start: Instant = clock.instant
  logger.info(s"ApplicationTimer demo: Starting application at $start.")
  // When the application starts, register a stop hook with the
  // ApplicationLifecycle object. The code inside the stop hook will
  // be run when the application stops.
  appLifecycle.addStopHook { () =>
    val stop: Instant = clock.instant
    val runningTime: Long = stop.getEpochSecond - start.getEpochSecond
    logger.info(s"ApplicationTimer demo: Stopping application at ${clock.instant} after ${runningTime}s.")
    Future.successful(())
  }
}
