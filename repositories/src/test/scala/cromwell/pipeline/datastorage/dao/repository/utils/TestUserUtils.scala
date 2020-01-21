package cromwell.pipeline.datastorage.dao.repository.utils

import cromwell.pipeline.datastorage.dto.{ Name, User, UserEmail, UserId }
import cromwell.pipeline.datastorage.dto.auth.Password
import cromwell.pipeline.utils.StringUtils
import cats.implicits._

object TestUserUtils {
  val userPassword = Password("-Pa$$w0rd-")

  def getDummyUser(
    uuid: UserId = UserId.random,
    password: String = userPassword,
    passwordSalt: String = "salt",
    firstName: Name = Name("FirstName"),
    lastName: Name = Name("Lastname"),
    active: Boolean = true
  ): User = {
    val passwordHash = StringUtils.calculatePasswordHash(password, passwordSalt)
    User(
      uuid,
      UserEmail(s"JohnDoe-${uuid.unwrap}@cromwell.com"),
      passwordHash,
      passwordSalt,
      firstName,
      lastName,
      None,
      active
    )
  }
}
