import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.Uri.Path
import com.fullfacing.keycloak4s.auth.core.PolicyBuilders
import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization
import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization.AuthRequest
import com.fullfacing.keycloak4s.core.models.enums.PolicyEnforcementModes
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.AuthTestData

class AuthorizationTests extends AnyFlatSpec with Matchers {

  implicit val cId: UUID = UUID.randomUUID()

  val config: AtomicReference[PathAuthorization] = new AtomicReference[PathAuthorization]()
  val config2: AtomicReference[PathAuthorization] = new AtomicReference[PathAuthorization]()


  "apply" should "successfully retrieve config.json and convert it into a PathAuthorization object" in {
    val auth = PolicyBuilders.buildPathAuthorization("config.json")

    auth.service shouldBe "api-test"
    auth.enforcementMode shouldBe PolicyEnforcementModes.Enforcing
    auth.paths.size shouldBe 4

    config.set(auth)
  }

  it should "successfully retrieve config2.json and convert it into a PathAuthorization object" in {
    val auth = PolicyBuilders.buildPathAuthorization("config2.json")

    auth.service shouldBe "api-test"
    auth.enforcementMode shouldBe PolicyEnforcementModes.Enforcing
    auth.paths.size shouldBe 5
    auth.paths shouldBe AuthTestData.config2.paths

    config2.set(auth)
  }

  "authorizeRequest" should "accept a valid request for which the user has access" in {
    val path      = Path("/v1/segment")
    val method    = HttpMethods.GET
    val userRoles = List("read")

    val result = config.get().authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))

    result shouldBe true
  }

  it should "recognise a UUID segment in the request and handle accordingly" in {
    val path      = Path("/v1/segment/689c4936-5274-4543-85d7-296cc456100b/anything/here")
    val method    = HttpMethods.HEAD
    val userRoles = List("wildcard-role")

    val result = config.get().authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))

    result shouldBe true
  }

  it should "reject a request where the user does not have the permissions expected for the matching path" in {
    val path      = Path("/v1/segment/689c4936-5274-4543-85d7-296cc456100b/anything/here")
    val method    = HttpMethods.HEAD
    val userRoles = List("delete")

    val result = config.get().authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))

    result shouldBe false
  }

  it should "reject a request where an invalid UUID segment is given where a valid one is expected" in {
    val path      = Path("/v1/segment/689c4936-5274-invalid-296cc456100b/anything/here")
    val method    = HttpMethods.HEAD
    val userRoles = List("wildcard-role")

    val result = config.get().authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))

    result shouldBe false
  }

  it should "reject a request where the matching path has no method matching the request with enforcementMode set to enforcing" in {
    val path      = Path("/v1/segment")
    val method    = HttpMethods.PATCH
    val userRoles = List("read")

    val result = config.get().authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))

    result shouldBe false
  }

  it should "reject a request where there is no matching path and enforcementMode is set to enforcing" in {
    val path      = Path("/v1/invalid")
    val method    = HttpMethods.GET
    val userRoles = List("read")

    val result = config.get().authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))

    result shouldBe false
  }

  it should "reject a request when the user does not have the required method permissions" in {
    val path      = Path("/v1/segment")
    val method    = HttpMethods.DELETE
    val userRoles = List("read", "write")

    val result = config.get().authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))

    result shouldBe false
  }

  it should "reject a request when the user does not have the required path permissions" in {
    val path      = Path("/v1/segment")
    val method    = HttpMethods.GET
    val userRoles = List("no")

    val result = config.get().authorizeRequest(AuthRequest(path.toString(), method.value, userRoles))

    result shouldBe false
  }
}
