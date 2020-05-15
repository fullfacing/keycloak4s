import java.time.Instant
import java.util.UUID

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.fullfacing.keycloak4s.auth.akka.http.directives.SecurityDirectives
import com.fullfacing.keycloak4s.auth.core.PolicyBuilders
import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
import com.nimbusds.jwt.SignedJWT
import org.json4s.jackson.Serialization._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.AuthTestData._
import utils.TestData._

class AuthDirectiveTests extends AnyFlatSpec with Matchers with ScalatestRouteTest with SecurityDirectives {

  val configEnforcing: PathAuthorization = PolicyBuilders.buildPathAuthorization("config.json")
  val configDisabled: PathAuthorization = PolicyBuilders.buildPathAuthorization("config_disabled.json")
  val configPermissive: PathAuthorization = PolicyBuilders.buildPathAuthorization("config_permissive.json")
  val configFullPathMatching: PathAuthorization = PolicyBuilders.buildPathAuthorization("config_full_path.json")

  def context: Directive1[UUID] = provide(UUID.randomUUID())
  val api1: Route =
    context { correlationId =>
      secure((configEnforcing, correlationId)) { _ =>
        complete("Authorized!")
      }
    }
  val api2: Route = secure(configDisabled) { _ => complete("Authorized!") }
  val api3: Route = secure(configPermissive) { _ => complete("Authorized!") }
  val api4: Route = pathPrefix("v1" / "segment") {
    secure(configEnforcing) { _ => complete("Authorized!") }
  }
  val api5: Route = pathPrefix("v1" / "segment") {
    secure(configFullPathMatching) { _ => complete("Authorized!") }
  }

  def permissionsBuilder(roles: List[String], service: String = "api-test"): String = write(service -> ("roles" -> roles))

  def validToken(roles: List[String], service: String = "api-test"): SignedJWT = createToken(
    withExp = Some(Instant.now().plusSeconds(60)),
    withIat = Some(Instant.now()),
    withIss = Some(validatorUri),
    withResourceAccess = Some(permissionsBuilder(roles, service))
  )

  def authHeader(token: String): HttpHeader = HttpHeader.parse("Authorization", s"Bearer $token") match {
    case HttpHeader.ParsingResult.Ok(h, Nil) => h
    case _ => throw new Exception
  }

  def Request(method: HttpMethod, path: String, token: String): HttpRequest =
    HttpRequest(
      method = method,
      uri    = path,
      headers = List(authHeader(token))
    )

  "secure" should "reject a request with no access token" in {
    Get("/v1/test/one") ~> api1 ~> check {
      responseAs[String] should not be "Authorized!"
      status shouldBe StatusCodes.Unauthorized
    }
  }

  it should "reject a request with an invalid access token" in {
    Request(GET, "/v1/test", "Invalid") ~> api1 ~> check {
      responseAs[String] should not be "Authorized!"
      status shouldBe StatusCodes.BadRequest
    }
  }

  it should "accept a request with a valid token that has all needed permissions" in {
    val token = validToken(List("admin")).serialize()
    val request = Request(GET, "/v1/test", token)

    request ~> api1 ~> check {
      responseAs[String] should be ("Authorized!")
      status shouldBe StatusCodes.OK
    }
  }

  it should "accept any request with a valid token when EnforcementMode is set to DISABLED" in {
    val token = validToken(List()).serialize()
    val request = Request(GET, "/v1/test", token)

    request ~> api2 ~> check {
      responseAs[String] should be ("Authorized!")
      status shouldBe StatusCodes.OK
    }
  }

  it should "accept any request with no matching path rule when EnforcementMode is set to PERMISSIVE" in {
    val token = validToken(List("an role")).serialize()
    val request = Request(GET, "/v1/notarealroute", token)

    request ~> api3 ~> check {
      responseAs[String] should be ("Authorized!")
      status shouldBe StatusCodes.OK
    }
  }

  it should "reject a request where the user does not have the required roles" in {
    val token = validToken(List("incorrect role")).serialize()
    val request = Request(POST, "/v1/segment/8fc41595-982f-4b00-a094-c71515cd1778/action", token)

    request ~> api3 ~> check {
      responseAs[String] should not be "Authorized!"
      status shouldBe StatusCodes.Forbidden
    }
  }

  it should "correctly authorize a request where the unmatched path is empty" in {
    val token = validToken(List("admin")).serialize()
    val request = Request(GET, "/v1/segment", token)

    request ~> api4 ~> check {
      responseAs[String] should be ("Authorized!")
      status shouldBe StatusCodes.OK
    }
  }

  it should "reject a request when the unmatched path does not match a policy rule and pathMatchingMode is set to Unmatched" in {
    val token = validToken(List("action-role")).serialize()
    val request = Request(POST, "/v1/segment/8fc41595-982f-4b00-a094-c71515cd1778/action", token)

    request ~> api4 ~> check {
      responseAs[String] should not be "Authorized!"
      status shouldBe StatusCodes.Forbidden
    }
  }

  it should "accept a request when the full path matches a policy rule and pathMatchingMode is set to Full" in {
    val token = validToken(List("action-role")).serialize()
    val request = Request(POST, "/v1/segment/8fc41595-982f-4b00-a094-c71515cd1778/action", token)

    request ~> api5 ~> check {
      responseAs[String] should be ("Authorized!")
      status shouldBe StatusCodes.OK
    }
  }
}