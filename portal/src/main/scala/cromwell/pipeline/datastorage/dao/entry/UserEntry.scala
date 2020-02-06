package cromwell.pipeline.datastorage.dao.entry

import cromwell.pipeline.datastorage.Profile
import cromwell.pipeline.datastorage.dto._

trait UserEntry {
  this: Profile =>

  import profile.api._
  import cats.implicits.catsStdShowForString

  private def iso[A, B](map: A => B, comap: B => A) = new Isomorphism(map, comap)
  implicit def uuidColumnType: Isomorphism[UUID, String] = iso[UUID, String](_.unwrap, UUID(_))
  implicit def emailColumnType: Isomorphism[UserEmail, String] = iso[UserEmail, String](_.unwrap, UserEmail(_))
  implicit def firstNameColumnType: Isomorphism[FirstName, String] = iso[FirstName, String](_.unwrap, FirstName(_))
  implicit def lastNameColumnType: Isomorphism[LastName, String] = iso[LastName, String](_.unwrap, LastName(_))

  class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def userId = column[UUID]("user_id", O.PrimaryKey)
    def email = column[UserEmail]("email")
    def passwordHash = column[String]("password_hash")
    def passwordSalt = column[String]("password_salt")
    def firstName = column[FirstName]("first_name")
    def lastName = column[LastName]("last_name")
    def profilePicture = column[ProfilePicture]("profile_picture")
    def active = column[Boolean]("active")
    def * = (userId, email, passwordHash, passwordSalt, firstName, lastName, profilePicture.?, active) <>
      ((User.apply _).tupled, User.unapply)
  }

  val users = TableQuery[UserTable]

  def getUserByIdAction = Compiled { userId: Rep[UUID] =>
    users.filter(_.userId === userId).take(1)
  }

  def getUserByEmailAction = Compiled { email: Rep[UserEmail] =>
    users.filter(_.email === email).take(1)
  }

  def getUsersByEmailAction(emailPattern: String) =
    users.filter(_.email.like(emailPattern)).result

  def addUserAction(user: User) = users.returning(users.map(_.userId)) += user

  def deactivateUserByEmail(email: UserEmail) = users.filter(_.email === email).map(_.active).update(false)

  def deactivateUserById(userId: UUID) = users.filter(_.userId === userId).map(_.active).update(false)

  def updateUser(updatedUser: User) =
    users
      .filter(_.userId === updatedUser.userId)
      .map(user => (user.email, user.firstName, user.lastName))
      .update((updatedUser.email, updatedUser.firstName, updatedUser.lastName))

  def updatePassword(updatedUser: User) =
    users
      .filter(_.userId === updatedUser.userId)
      .map(user => (user.passwordHash, user.passwordSalt))
      .update((updatedUser.passwordHash, updatedUser.passwordSalt))
}
