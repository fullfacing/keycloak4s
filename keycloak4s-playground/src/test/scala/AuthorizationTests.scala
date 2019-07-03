import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.Uri.Path
import com.fullfacing.keycloak4s.auth.akka.http.authorization.{PathAuthorization, PolicyEnforcement}
import com.fullfacing.keycloak4s.core.models.enums.PolicyEnforcementModes
import org.scalatest.{FlatSpec, Matchers}

class AuthorizationTests extends FlatSpec with Matchers {

  implicit val cId: UUID = UUID.randomUUID()

  val config: AtomicReference[PathAuthorization] = new AtomicReference[PathAuthorization]()

  "apply" should "successfully retrieve the json resource and convert it into the PathAuthorization object" in {
    val auth = PolicyEnforcement.buildPathAuthorization("config.json")

    auth.service shouldBe "api-test"
    auth.enforcementMode shouldBe PolicyEnforcementModes.Enforcing
    auth.paths.size shouldBe 3

    config.set(auth)
  }

  "authorizeRequest" should "" in {
    val path      = Path("/v1/segment")
    val method    = HttpMethods.GET
    val userRoles = List("read")

    val result = config.get().authorizeRequest(path, method, userRoles)

    result shouldBe true
  }
}
