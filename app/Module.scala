//import actors.HelloActor

import actors.PredictActor
import com.google.inject.AbstractModule
//import config.ConfiguredActor
import play.api.libs.concurrent.AkkaGuiceSupport
import services.ApplicationTimer

import java.time.Clock

/**
 * Guice Example
 *
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    bindActor[PredictActor]("configured-actor")
  }

}
