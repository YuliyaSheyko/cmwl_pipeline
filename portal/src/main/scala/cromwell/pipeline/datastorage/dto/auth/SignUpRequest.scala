package cromwell.pipeline.datastorage.dto.auth

import cats.data.{ NonEmptyChain, Validated }
import cromwell.pipeline.datastorage.dto.{ FirstName, LastName, UserEmail }
import cromwell.pipeline.utils.validator.Wrapped
import play.api.libs.json.{ Format, Json, OFormat }
import slick.lifted.MappedTo

final case class SignUpRequest(email: UserEmail, password: Password, firstName: FirstName, lastName: LastName)

object SignUpRequest {
  implicit val signUpRequestFormat: OFormat[SignUpRequest] = Json.format[SignUpRequest]
}
final class Password private (override val unwrap: String) extends AnyVal with Wrapped[String] with MappedTo[String] {
  override def value: String = unwrap
}
object Password extends Wrapped.Companion {
  type Type = String
  type Wrapper = Password
  type Error = String
  implicit lazy val passwordFormat: Format[Password] = Json.valueFormat[Password]
  override protected def create(value: String): Password = new Password(value)
  override protected def validate(value: String): ValidationResult[String] = Validated.cond(
    value.matches("(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$"),
    value,
    NonEmptyChain.one("Invalid password")
  )
}
