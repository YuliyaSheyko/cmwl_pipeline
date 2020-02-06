package cromwell.pipeline.datastorage.dto

import cats.data.{ NonEmptyChain, Validated }
import cromwell.pipeline.utils.validator.Wrapped
import play.api.libs.json.{ Format, Json, OFormat }
import slick.lifted.MappedTo

final case class User(
  userId: UUID,
  email: UserEmail,
  passwordHash: String,
  passwordSalt: String,
  firstName: FirstName,
  lastName: LastName,
  profilePicture: Option[ProfilePicture] = None,
  active: Boolean = true
)

object User {
  implicit lazy val userFormat: OFormat[User] = Json.format[User]
}

final case class ProfilePicture(value: Array[Byte]) extends MappedTo[Array[Byte]]

object ProfilePicture {
  implicit lazy val profilePictureFormat: OFormat[ProfilePicture] = Json.format[ProfilePicture]
}

final class UserEmail private (override val unwrap: String) extends AnyVal with Wrapped[String]

object UserEmail extends Wrapped.Companion {
  type Type = String
  type Wrapper = UserEmail
  type Error = String
  implicit lazy val emailFormat: Format[UserEmail] = Json.valueFormat[UserEmail]
  override protected def create(value: String): UserEmail = new UserEmail(value)
  override protected def validate(value: String): ValidationResult[String] = Validated.cond(
    value.matches("^[^@]+@[^\\.]+\\..+$"),
    value,
    NonEmptyChain.one("Invalid email")
  )
}

final class FirstName private (override val unwrap: String) extends AnyVal with Wrapped[String]

object FirstName extends Wrapped.Companion {
  type Type = String
  type Wrapper = FirstName
  type Error = String
  implicit lazy val nameFormat: Format[FirstName] = Json.valueFormat[FirstName]
  override protected def create(value: String): FirstName = new FirstName(value)
  override protected def validate(value: String): ValidationResult[String] = Validated.cond(
    value.matches("^[a-zA-Z]+$"),
    value,
    NonEmptyChain.one("Invalid first name")
  )
}

final class LastName private (override val unwrap: String) extends AnyVal with Wrapped[String]

object LastName extends Wrapped.Companion {
  type Type = String
  type Wrapper = LastName
  type Error = String
  implicit lazy val nameFormat: Format[LastName] = Json.valueFormat[LastName]
  override protected def create(value: String): LastName = new LastName(value)
  override protected def validate(value: String): ValidationResult[String] = Validated.cond(
    value.matches("^[a-zA-Z]+$"),
    value,
    NonEmptyChain.one("Invalid last name")
  )
}

final class UUID(override val unwrap: String) extends AnyVal with Wrapped[String]

object UUID extends Wrapped.Companion {
  type Type = String
  type Wrapper = UUID
  type Error = String
  val pattern: String = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
  implicit val uuidFormat: Format[UUID] = Json.valueFormat[UUID]
  override protected def create(value: String): UUID = new UUID(value)
  override protected def validate(value: String): ValidationResult[String] = Validated.cond(
    value.matches(pattern),
    value,
    NonEmptyChain.one("Invalid last name")
  )

  def random: UUID = new UUID(java.util.UUID.randomUUID().toString)
}
