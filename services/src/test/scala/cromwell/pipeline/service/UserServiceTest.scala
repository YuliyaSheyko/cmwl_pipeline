package cromwell.pipeline.service

import cats.implicits._
import cromwell.pipeline.datastorage.dao.repository.UserRepository
import cromwell.pipeline.datastorage.dao.repository.utils.TestUserUtils
import cromwell.pipeline.datastorage.dto.user.{ PasswordUpdateRequest, UserUpdateRequest }
import cromwell.pipeline.datastorage.dto.{ Name, User, UserEmail, UserId, UserNoCredentials }
import cromwell.pipeline.utils.StringUtils
import org.mockito.Mockito._
import org.scalatest.{ AsyncWordSpec, Matchers }
import org.scalatestplus.mockito.MockitoSugar
import StringUtils._
import cromwell.pipeline.datastorage.dto.auth.Password

import scala.concurrent.Future

class UserServiceTest extends AsyncWordSpec with Matchers with MockitoSugar {
  private val userRepository: UserRepository = mock[UserRepository]
  private val userService: UserService = new UserService(userRepository)
  private val dummyUser: User = TestUserUtils.getDummyUser()
  private val userId: UserId = UserId.random
  private lazy val userByEmailRequest: String = "@gmail"
  private lazy val userRepositoryResp = Seq(dummyUser)
  private lazy val userServiceResp: Seq[User] = Seq(dummyUser)

  "UserService" when {

    "invoke UserService" should {
      "get userResponse sequence from users sequence" taggedAs Service in {

        when(userRepository.getUsersByEmail(userByEmailRequest)).thenReturn(Future.successful(userRepositoryResp))
        userService.getUsersByEmail(userByEmailRequest).map { result =>
          result shouldBe userServiceResp
        }
      }
    }

    "deactivateUserById" should {
      "returns user's entity with false value" taggedAs Service in {
        val user: User = dummyUser.copy(userId = userId)

        when(userRepository.deactivateUserById(userId)).thenReturn(Future.successful(1))
        when(userRepository.getUserById(userId)).thenReturn(Future(Some(user)))

        val response = UserNoCredentials.fromUser(user)
        userService.deactivateUserById(userId).map { result =>
          result shouldBe Some(response)
        }
      }
      "return None if user wasn't found by Id" taggedAs Service in {
        when(userRepository.deactivateUserById(userId)).thenReturn(Future.successful(0))
        when(userRepository.getUserById(userId)).thenReturn(Future(None))

        userService.deactivateUserById(userId).map { result =>
          result shouldBe None
        }
      }
    }

    "updateUser" should {
      "returns success if database handles query" taggedAs Service in {
        val userId = UserId.random
        val updatedUser =
          dummyUser.copy(
            email = UserEmail("updatedEmail@gmail.com"),
            firstName = Name("updatedFirstName"),
            lastName = Name("updatedLastName")
          )
        val request =
          UserUpdateRequest(UserEmail("updatedEmail@gmail.com"), Name("updatedFirstName"), Name("updatedLastName"))

        when(userRepository.getUserById(userId)).thenReturn(Future(Some(dummyUser)))
        when(userRepository.updateUser(updatedUser)).thenReturn(Future.successful(1))

        userService.updateUser(userId, request).map { result =>
          result shouldBe 1
        }
      }
    }

    "updatePassword" should {
      "returns success if database handles query" taggedAs Service in {
        val id = UserId.random
        val salt = "salt"
        val user =
          User(
            id,
            UserEmail("email@cromwell.com"),
            calculatePasswordHash("Password123", salt),
            salt,
            Name("Name"),
            Name("LastName")
          )
        val request = PasswordUpdateRequest(
          Password("Password123"),
          Password("newPassword123"),
          Password("newPassword123")
        )
        val updatedUser =
          user.copy(passwordHash = StringUtils.calculatePasswordHash("newPassword123", salt), passwordSalt = salt)

        when(userRepository.getUserById(UserId(id))).thenReturn(Future(Some(user)))
        when(userRepository.updatePassword(updatedUser)).thenReturn(Future.successful(1))

        userService.updatePassword(id, request, salt).map { result =>
          result shouldBe 1
        }
      }
    }
  }
}
