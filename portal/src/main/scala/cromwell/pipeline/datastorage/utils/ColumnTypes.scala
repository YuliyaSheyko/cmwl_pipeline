package cromwell.pipeline.datastorage.utils

import cromwell.pipeline.datastorage.dto.{ FirstName, LastName, UUID, UserEmail }
import cromwell.pipeline.datastorage.Profile

trait ColumnTypes {
  this: Profile =>

  import profile.api._
  import cats.implicits.catsStdShowForString

  implicit def uuidColumnType: Isomorphism[UUID, String] = iso[UUID, String](_.unwrap, UUID(_))
  implicit def emailColumnType: Isomorphism[UserEmail, String] = iso[UserEmail, String](_.unwrap, UserEmail(_))
  implicit def firstNameColumnType: Isomorphism[FirstName, String] = iso[FirstName, String](_.unwrap, FirstName(_))
  implicit def lastNameColumnType: Isomorphism[LastName, String] = iso[LastName, String](_.unwrap, LastName(_))

  private def iso[A, B](map: A => B, comap: B => A) = new Isomorphism(map, comap)
}
