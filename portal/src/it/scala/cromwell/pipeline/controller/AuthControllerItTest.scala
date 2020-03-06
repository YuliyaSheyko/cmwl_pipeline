package cromwell.pipeline.controller

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ ForAllTestContainer, PostgreSQLContainer }
import com.typesafe.config.Config
import cromwell.pipeline.controller.AuthController._
import cromwell.pipeline.ApplicationComponents
import cromwell.pipeline.datastorage.dao.repository.utils.TestUserUtils
import cromwell.pipeline.datastorage.dto.auth.{ Password, SignInRequest, SignUpRequest }
import cromwell.pipeline.datastorage.dto.{ User, UserEmail }
import cromwell.pipeline.utils.TestContainersUtils
import org.scalatest.compatible.Assertion
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.{ Matchers, WordSpec }
import play.api.libs.json.Json
import cats.implicits._

class AuthControllerItTest extends WordSpec with Matchers with ScalatestRouteTest with ForAllTestContainer {

  override val container: PostgreSQLContainer = TestContainersUtils.getPostgreSQLContainer()
  container.start()
  implicit val config: Config = TestContainersUtils.getConfigForPgContainer(container)
  private val components: ApplicationComponents = new ApplicationComponents()

  import components.controllerModule.authController
  import components.datastorageModule.userRepository

  override protected def beforeAll(): Unit =
    components.datastorageModule.pipelineDatabaseEngine.updateSchema()

  private val dummyUser: User = TestUserUtils.getDummyUser()
  private val password: Password = Password("-Pa$$w0rd-")

  "AuthController" when {

    "signIn" should {

      "return token headers if user exists" in {
        val dummyUser: User = TestUserUtils.getDummyUser()
        whenReady(userRepository.addUser(dummyUser)) { _ =>
          val signInRequest = SignInRequest(dummyUser.email, password)
          val httpEntity = HttpEntity(`application/json`, Json.stringify(Json.toJson(signInRequest)))

          Post("/auth/signIn", httpEntity) ~> authController.route ~> check {
            status shouldBe StatusCodes.OK
            checkAuthTokens
          }
        }
      }
    }

    "signUp" should {

      "return token headers if user was successfully registered" in {
        val signUpRequest =
          SignUpRequest(UserEmail("AnotherJohnDoe-@cromwell.com"), password, dummyUser.firstName, dummyUser.lastName)
        val httpEntity = HttpEntity(`application/json`, Json.stringify(Json.toJson(signUpRequest)))

        Post("/auth/signUp", httpEntity) ~> authController.route ~> check {
          status shouldBe StatusCodes.OK
          checkAuthTokens
        }
      }
    }

    "refresh" should {

      "return updated token headers if refresh token was valid and active" in {
        val dummyUser: User = TestUserUtils.getDummyUser()
        whenReady(userRepository.addUser(dummyUser)) { _ =>
          val signInRequest = SignInRequest(dummyUser.email, password)
          val httpEntity = HttpEntity(`application/json`, Json.stringify(Json.toJson(signInRequest)))

          Post("/auth/signIn", httpEntity) ~> authController.route ~> check {
            header(RefreshTokenHeader).map { refreshTokenHeader =>
              Get(s"/auth/refresh?refreshToken=${refreshTokenHeader.value}") ~> authController.route ~> check {
                status shouldBe StatusCodes.OK
                checkAuthTokens
              }
            }.getOrElse(fail)
          }
        }
      }
    }

  }

  private def checkAuthTokens: Assertion =
    Seq(AccessTokenHeader, RefreshTokenHeader, AccessTokenExpirationHeader).forall(header(_).isDefined) shouldBe true

}
