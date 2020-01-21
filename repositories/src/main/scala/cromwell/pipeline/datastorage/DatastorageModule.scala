package cromwell.pipeline.datastorage

import cromwell.pipeline.database.PipelineDatabaseEngine
import cromwell.pipeline.datastorage.dao.ProjectEntry
import cromwell.pipeline.datastorage.dao.entry.UserEntry
import cromwell.pipeline.datastorage.dao.repository.{ ProjectRepository, UserRepository }
import cromwell.pipeline.datastorage.dto.{ Name, UserEmail, UserId }
import cromwell.pipeline.datastorage.utils.auth.{ AuthUtils, SecurityDirective }
import cromwell.pipeline.datastorage.utils.validator.Wrapped
import cromwell.pipeline.utils.ApplicationConfig
import slick.jdbc.JdbcProfile
import slick.lifted.StringColumnExtensionMethods

class DatastorageModule(applicationConfig: ApplicationConfig) {

  lazy val authUtils: AuthUtils = new AuthUtils(applicationConfig.authConfig)
  lazy val securityDirective: SecurityDirective = new SecurityDirective(applicationConfig.authConfig)
  lazy val pipelineDatabaseEngine: PipelineDatabaseEngine = new PipelineDatabaseEngine(applicationConfig.config)
  lazy val profile: JdbcProfile = pipelineDatabaseEngine.profile
  lazy val databaseLayer: DatabaseLayer = new DatabaseLayer(profile)

  lazy val userRepository: UserRepository =
    new UserRepository(pipelineDatabaseEngine, databaseLayer)
  lazy val projectRepository: ProjectRepository =
    new ProjectRepository(pipelineDatabaseEngine, databaseLayer)
}

trait Profile {
  val profile: JdbcProfile
  object Implicits {

    import profile.api._
    import cats.implicits.catsStdShowForString
    import scala.language.implicitConversions

    implicit def uuidIso: Isomorphism[UserId, String] = iso[UserId, String](_.unwrap, UserId(_))
    implicit def emailIso: Isomorphism[UserEmail, String] = iso[UserEmail, String](_.unwrap, UserEmail(_))
    implicit def nameIso: Isomorphism[Name, String] = iso[Name, String](_.unwrap, Name(_))

    implicit def wrappedStringColumnExtension[T <: Wrapped[String]](c: Rep[T]): StringColumnExtensionMethods[String] =
      new StringColumnExtensionMethods[String](c.asInstanceOf[Rep[String]])

    private def iso[A, B](map: A => B, comap: B => A) = new Isomorphism(map, comap)
  }
}

class DatabaseLayer(val profile: JdbcProfile) extends Profile with UserEntry with ProjectEntry
