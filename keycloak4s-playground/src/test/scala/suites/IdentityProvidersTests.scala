package suites

import java.util.concurrent.atomic.AtomicReference

import cats.data.EitherT
import com.fullfacing.keycloak4s.core.models.{IdentityProvider, IdentityProviderMapper}
import com.fullfacing.keycloak4s.core.models.enums.{MapperTypes, ProviderTypes}
import monix.eval.Task
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

@DoNotDiscover
class IdentityProvidersTests extends IntegrationSpec {

  /**
   * Calls in the IdentityProviders service not covered by the below tests:
   *
   * exportBrokerConfig - Functionality is unclear (GET call that returns a 204 NoContent)
   */

  val storedMapper: AtomicReference[IdentityProviderMapper] = new AtomicReference[IdentityProviderMapper]()

  val savedIdentityProvider: AtomicReference[IdentityProvider] = new AtomicReference[IdentityProvider]()

  def mapToUpdate(idp: IdentityProvider): IdentityProvider.Update = {
    IdentityProvider.Update(
      alias = idp.alias,
      providerId = Some(idp.providerId),
      config = Some(idp.config),
      addReadTokenRoleOnCreate = Some(idp.addReadTokenRoleOnCreate),
      displayName = idp.displayName,
      enabled = Some(idp.enabled),
      firstBrokerLoginFlowAlias = Some(idp.firstBrokerLoginFlowAlias),
      internalId = Some(idp.internalId),
      linkOnly = Some(idp.linkOnly),
      postBrokerLoginFlowAlias = idp.postBrokerLoginFlowAlias,
      storeToken = Some(idp.storeToken),
      trustEmail = Some(idp.trustEmail),
    )
  }

  "fetch" should "return an empty list of identity provider" in {
    idProvService.fetch().map(_.map { providers =>
      providers shouldBe empty
    })
  }.shouldReturnSuccess

  "create" should "create an identity provider" in {
    val config = Map(
      "authorizationUrl"  -> "http://localhost/test/oidc",
      "tokenUrl"          -> "http://localhost/test/oidc",
      "clientId"          -> "test_id",
      "clientSecret"      -> "test_secret",
      "clientAuthMethod"  -> "client_secret_basic"
    )

    val identityProvider = IdentityProvider.Create(
      config      = config,
      alias       = "test_oidc",
      providerId  = ProviderTypes.Oidc,
      trustEmail  = Some(false),
      internalId  = Some("somestring")
    )

    for {
      _       <- EitherT(idProvService.create(identityProvider))
      idProvs <- EitherT(idProvService.fetch())
      idProv  <- EitherT.fromOption[Task](idProvs.headOption, Errors.NO_IDPROVS_FOUND)
    } yield {
      savedIdentityProvider.set(idProv)
      idProv.alias shouldBe "test_oidc"
    }
  }.value.shouldReturnSuccess

  "fetch" should "also be able to retrieve an Identity Provider by its alias" in {
    idProvService.fetchByAlias("test_oidc").map(_.map { idProv =>
      idProv.config.get("clientId") shouldBe Some("test_id")
    })
  }.shouldReturnSuccess

  "fetchProviderType" should "retrieve the name and id of a Provider type" in {
    idProvService.fetchProviderType(ProviderTypes.Oidc).map(_.map { info =>
      info.name shouldBe "OpenID Connect v1.0"
    })
  }.shouldReturnSuccess

  "update" should "update an existing Identity Provider" in {
    val update = mapToUpdate(savedIdentityProvider.get())
      .copy(trustEmail = Some(true))

    for {
      _       <- EitherT(idProvService.update("test_oidc", update))
      idProv  <- EitherT(idProvService.fetchByAlias("test_oidc"))
    } yield idProv.trustEmail shouldBe true
  }.value.shouldReturnSuccess

  "fetchManagementPermissions" should "return the management permissions of an Identity Provider" in {
    idProvService.fetchManagementPermissions("test_oidc").map(_.map { mp =>
      mp.enabled shouldBe false
    })
  }.shouldReturnSuccess

  "ManagementPermissions" should "update the management permissions of an Identity Provider" in {
    (for {
      ep <- EitherT(idProvService.enableManagementPermissions("test_oidc"))
      dp <- EitherT(idProvService.disableManagementPermissions("test_oidc"))
    } yield {
      ep.enabled should equal(true)
      dp.enabled should equal(false)
    }).value
  }.shouldReturnSuccess

  "fetchMapperTypes" should "retrieve the mapper types for the specified Identity Provider" in {
    idProvService.fetchMapperTypes("test_oidc").map(_.map { mapperTypes =>
      mapperTypes shouldNot be (empty)
      mapperTypes.foreach(println)
    })
  }.shouldReturnSuccess

  "fetchMappers" should "retrieve an empty list of Identity Provider mappers" in {
    idProvService.fetchMappers("test_oidc").map(_.map { mappers =>
      mappers shouldBe empty
    })
  }.shouldReturnSuccess

  "createMapper" should "create a mapper for a specified Identity Provider" in {
    val mapper = IdentityProviderMapper.Create(
      name                    = "test_mapper",
      identityProviderMapper  = MapperTypes.HardcodedUserSessionAttribute,
      identityProviderAlias   = "test_oidc"
    )

    for {
      _       <- EitherT(idProvService.createMapper("test_oidc", mapper))
      mappers <- EitherT(idProvService.fetchMappers("test_oidc"))
      mapper  <- EitherT.fromOption[Task](mappers.headOption, Errors.NO_MAPPERS_FOUND)
    } yield {
      mapper.name shouldBe "test_mapper"
      storedMapper.set(mapper)
    }
  }.value.shouldReturnSuccess

  "fetchMapper" should "retrieve a mapper by ID for a specified Identity Provider" in {
    idProvService.fetchMapper("test_oidc", storedMapper.get().id).map(_.map { mapper =>
      mapper.name shouldBe "test_mapper"
    })
  }.shouldReturnSuccess

  "updateMapper" should "update a specified mapper for a specified Identity Provider" in {
    val update = IdentityProviderMapper.Update(
      id                      = storedMapper.get().id,
      identityProviderAlias   = "test_oidc",
      identityProviderMapper  = MapperTypes.HardcodedUserSessionAttribute,
      config                  = Map(
        "attribute.value" -> "test_value",
        "attribute"       -> "test_attribute"
      )
    )

    for {
      _       <- EitherT(idProvService.updateMapper("test_oidc", storedMapper.get().id, update))
      mapper  <- EitherT(idProvService.fetchMapper("test_oidc", storedMapper.get().id))
    } yield {
      mapper.config.get("attribute") shouldBe Some("test_attribute")
      mapper.config.get("attribute.value") shouldBe Some("test_value")
    }
  }.value.shouldReturnSuccess

  "deleteMapper" should "delete a specified Identity Provider mapper" in {
    idProvService.deleteMapper("test_oidc", storedMapper.get().id)

    for {
      _       <- EitherT(idProvService.deleteMapper("test_oidc", storedMapper.get().id))
      mappers <- EitherT(idProvService.fetchMappers("test_oidc"))
    } yield mappers shouldBe empty
  }.value.shouldReturnSuccess

  "delete" should "delete an Identity Provider by its alias" in {
    for {
      _       <- EitherT(idProvService.delete("test_oidc"))
      idProvs <- EitherT(idProvService.fetch())
    } yield idProvs shouldBe empty
  }.value.shouldReturnSuccess
}
