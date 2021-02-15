package suites.authz

import com.fullfacing.keycloak4s.authz.monix.bio.models.AuthorizationRequest
import org.scalatest.DoNotDiscover
import suites.authz.IoIntegration._

import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class AuthorizationTests extends IoIntegration {

  val token: AtomicReference[String] = new AtomicReference[String]()

  "authorize" should "successfully return an authorization response" in {
    authorization
      .flatMap(_.authorize(AuthorizationRequest()))
      .map(response => token.set(response.access_token))
      .shouldReturnSuccess
  }

  "authorizeWithDecisionResponse" should "successfully return an authorization decision" in {
    authorization
      .flatMap(_.authorizeWithDecisionResponse(AuthorizationRequest()))
      .shouldReturnSuccess
  }

  "authorizeWithPermissionResponse" should "successfully return a list of permissions" in {
    authorization
      .flatMap(_.authorizeWithPermissionResponse(AuthorizationRequest()))
      .shouldReturnSuccess
  }

  "introspect" should "successfully return the token details" in {
    authzClient
      .flatMap(_.introspectToken(token.get()))
      .shouldReturnSuccess
  }
}
