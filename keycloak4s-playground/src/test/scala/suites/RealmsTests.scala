package suites

import cats.data.EitherT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.models.enums.EventTypes
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class RealmsTests extends IntegrationSpec {

  /**
   * Calls in the Realms service not covered by the below tests:
   *
   * logoutAll                  - Broken on the Keycloak server itself.
   * pushRevocation             - Broken on the Keycloak server itself.
   * clientDescriptionConverter - The exact purpose or use cases for the call is unknown.
   * partialImport              - The structure/contents of the JSON file to be POSTed is not sufficiently explained.
   * testLdapConnection         - Requires setup not feasible for testing.
   * testSmtpConnection         - Requires setup not feasible for testing.
   */

  /* References for storing tests results to be used in subsequent tests. **/
  val storedScopeId: AtomicReference[UUID]    = new AtomicReference[UUID]()
  val storedGroup: AtomicReference[Group]     = new AtomicReference[Group]()
  val storedTokenId: AtomicReference[UUID]    = new AtomicReference[UUID]()
  val storedBoolean: AtomicReference[Boolean] = new AtomicReference[Boolean]()

  "create" should "POST a new Realm" in {
    val realm = Realm.Create(
      id                        = "test_realm",
      realm                     = "test_realm",
      eventsEnabled             = Some(true),
      eventsListeners           = Some(List("jboss-logging")),
      adminEventsEnabled        = Some(true),
      adminEventsDetailsEnabled = Some(true),
      enabledEventTypes         = Some(EventTypes.values.toList)
    )

    for {
      _ <- EitherT(realmService.create(realm))
      _ <- EitherT(realmService.fetch("test_realm"))
    } yield ()
  }.value.shouldReturnSuccess

  "createAndRetrieve" should "create a Realm and subsequently return it" in {
    realmService.createAndRetrieve(Realm.Create(id = "test_realm2", realm = "test_realm2"))
  }.shouldReturnSuccess

  "fetch" should "retrieve the Realm as configured in KeycloakClient" in {
    realmService.fetch().map(_.map { realm =>
      realm.realm shouldBe "master"
    })
  }.shouldReturnSuccess

  it should "also be able to retrieve a Realm with a specified name" in {
    realmService.fetch("test_realm").map(_.map { realm =>
      realm.id shouldBe "test_realm"
    })
  }.shouldReturnSuccess

  "fetchAll" should "retrieve all Realms" in {
    realmService.fetchAll().map(_.map { realms =>
      realms.map(_.id) should contain allOf ("test_realm", "test_realm2")
      realms.map(_.realm) should contain ("master")
    })
  }.shouldReturnSuccess

  "update" should "update a Realm" in {
    val update = Realm.Update(enabled = Some(true))

    for {
      _     <- EitherT(realmService.update(update, "test_realm"))
      realm <- EitherT(realmService.fetch("test_realm"))
    } yield realm.enabled shouldBe true
  }.value.shouldReturnSuccess

  "fetchAdminEvents" should "retrieve a non-empty list of admin events" in {
    realmService.fetchAdminEvents(realm = "test_realm").map(_.map { events =>
      events shouldNot be (empty)
    })
  }.shouldReturnSuccess

  "deleteAdminEvents" should "delete all admin events that have been logged" in {
    for {
      _       <- EitherT(realmService.deleteAdminEvents(realm = "test_realm"))
      events  <- EitherT(realmService.fetchAdminEvents(realm = "test_realm"))
    } yield events shouldBe empty
  }.value.shouldReturnSuccess

  "fetchClientSessionStats" should "retrieve a non-empty list of client session statistics" in {
    realmService.fetchClientSessionStats("master").map(_.map { stats =>
      stats shouldNot be (empty)
    })
  }.shouldReturnSuccess

  "fetchDefaultClientScopes" should "retrieve a non-empty list of default client scopes" in {
    for {
      scopes  <- EitherT(realmService.fetchDefaultClientScopes("test_realm"))
      scope   <- EitherT.fromOption[IO](scopes.headOption, Errors.SCOPE_NOT_FOUND)
    } yield {
      scopes shouldNot be (empty)
      storedScopeId.set(scope.id)
    }
  }.value.shouldReturnSuccess

  "unassignDefaultClientScope" should "unassign a default scope" in {
    for {
      _       <- EitherT(realmService.unassignClientScopeAsDefault(storedScopeId.get(), "test_realm"))
      scopes  <- EitherT(realmService.fetchDefaultClientScopes("test_realm"))
    } yield scopes.map(_.id) shouldNot contain (storedScopeId.get())
  }.value.shouldReturnSuccess

  "assignDefaultClientScope" should "assign a default scope" in {
    for {
      _       <- EitherT(realmService.assignClientScopeAsDefault(storedScopeId.get(), "test_realm"))
      scopes  <- EitherT(realmService.fetchDefaultClientScopes("test_realm"))
    } yield scopes.map(_.id) should contain (storedScopeId.get())
  }.value.shouldReturnSuccess

  "fetchDefaultGroups" should "retrieve a empty list of default groups" in {
    realmService.fetchDefaultGroups().map(_.map { groups =>
      groups shouldBe empty
    })
  }.shouldReturnSuccess

  "assignGroupAsDefault" should "assign a specified group as default" in {
    for {
      _       <- EitherT(groupService.create(Group.Create(name = "test_group")))
      opt     <- EitherT(groupService.fetch()).map(_.find(_.name == "test_group"))
      group   <- EitherT.fromOption[IO](opt, Errors.NO_GROUPS_FOUND)
      _       <- EitherT(realmService.assignGroupAsDefault(group.id))
      fetched <- EitherT(realmService.fetchDefaultGroups())
    } yield {
      fetched.map(_.name) should contain ("test_group")
      storedGroup.set(group)
    }
  }.value.shouldReturnSuccess

  "fetchGroupByPath" should "retrieve a Group in a Realm by its path" in {
    realmService.fetchGroupByPath(storedGroup.get().path).map(_.map { group =>
      group.id shouldBe storedGroup.get().id
    })
  }.shouldReturnSuccess

  "unassignGroupAsDefault" should "unassign a specified group as default" in {
    for {
      _       <- EitherT(realmService.unassignGroupAsDefault(storedGroup.get().id))
      _       <- EitherT(groupService.delete(storedGroup.get().id))
      fetched <- EitherT(realmService.fetchDefaultGroups())
    } yield fetched.map(_.name) shouldNot contain ("test_group")
  }.value.shouldReturnSuccess

  "fetchOptionalClientScopes" should "retrieve a non-empty list of assigned optional client scopes" in {
    for {
      scopes  <- EitherT(realmService.fetchOptionalClientScopes("test_realm"))
      scope   <- EitherT.fromOption[IO](scopes.headOption, Errors.SCOPE_NOT_FOUND)
    } yield {
      scopes shouldNot be (empty)
      storedScopeId.set(scope.id)
    }
  }.value.shouldReturnSuccess

  "unassignOptionalClientScope" should "unassign a optional scope" in {
    for {
      _       <- EitherT(realmService.unassignClientScopeAsOptional(storedScopeId.get(), "test_realm"))
      scopes  <- EitherT(realmService.fetchOptionalClientScopes("test_realm"))
    } yield scopes.map(_.id) shouldNot contain (storedScopeId.get())
  }.value.shouldReturnSuccess

  "assignOptionalClientScope" should "assign a optional scope" in {
    for {
      _       <- EitherT(realmService.assignClientScopeAsOptional(storedScopeId.get(), "test_realm"))
      scopes  <- EitherT(realmService.fetchOptionalClientScopes("test_realm"))
    } yield scopes.map(_.id) should contain (storedScopeId.get())
  }.value.shouldReturnSuccess

  /* Difficult to properly test, event logs cannot be forced through the Admin API. **/
  "fetchEvents" should "retrieve a list of login events" in {
    realmService.fetchEvents()
  }.shouldReturnSuccess

  "deleteAllEvents" should "delete all logging events" in {
    for {
      _       <- EitherT(realmService.deleteAllEvents())
      events  <- EitherT(realmService.fetchEvents())
    } yield events should be (empty)
  }.value.shouldReturnSuccess

  "fetchEventsConfig" should "retrieve the events configuration" in {
    realmService.fetchEventsConfig().map(_.map { config =>
      storedBoolean.set(config.eventsEnabled)
    })
  }.shouldReturnSuccess

  "updateEventsConfig" should "update the events configuration" in {
    val update = RealmEventsConfig.Update(eventsEnabled = Some(!storedBoolean.get()))

    for {
      _       <- EitherT(realmService.updateEventsConfig(update))
      config  <- EitherT(realmService.fetchEventsConfig())
    } yield config.eventsEnabled shouldNot be (storedBoolean.get())
  }.value.shouldReturnSuccess

  "partialExport" should "partially export a Realm" in {
    realmService.partialExport().map(_.map { export =>
      export.realm shouldBe "master"
    })
  }.shouldReturnSuccess

  "removeUserSession" should "invalidate a specified user session" in {
    for {
      id        <- EitherT(clientService.fetch(clientId = Some("account"))).map(_.head.id)
      option    <- EitherT(clientService.fetchUserSessions(id)).map(_.headOption)
      session   <- EitherT.fromOption[IO](option, Errors.NO_SESSIONS_FOUND)
      _         <- EitherT(realmService.removeUserSession(session.id))
      sessions  <- EitherT(clientService.fetchUserSessions(id))
    } yield (sessions.map(_.id), session.id)
  }.value.map {
    case Left(l)          => l.getMessage.toLowerCase shouldBe "no sessions found."
    case Right((ids, id)) => ids shouldNot contain (id)
  }.unsafeToFuture

  "fetchUsersManagementPermissions" should "retrieve an object stating that management permissions are disabled" in {
    realmService.fetchUsersManagementPermissions("test_realm").map(_.map { permissions =>
      permissions.enabled shouldBe false
    })
  }.shouldReturnSuccess

  "UsersManagementPermissions" should "update the user management permissions" in {
    (for {
      ep <- EitherT(realmService.enableUsersManagementPermissions("test_realm"))
      dp <- EitherT(realmService.disableUsersManagementPermissions("test_realm"))
    } yield {
      ep.enabled should equal(true)
      dp.enabled should equal(false)
    }).value
  }.shouldReturnSuccess

  "fetchClientRegistrationPolicyProviders" should "retrieve a non-empty list of client registration polices" in {
    realmService.fetchClientRegistrationPolicyProviders().map(_.map { policies =>
      policies shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "fetchInitialAccessTokens" should "retrieve an empty list of initial access tokens" in {
    realmService.fetchInitialAccessTokens("test_realm").map(_.map { tokens =>
      tokens should be (empty)
    })
  }.shouldReturnSuccess

  "createInitialAccessToken" should "create an initial access token" in {
    val config = ClientInitialAccess.Create(count = Some(5), expiration = Some(86400))
    realmService.createInitialAccessToken(config, "test_realm").shouldReturnSuccess

    for {
      _       <- EitherT(realmService.createInitialAccessToken(config, "test_realm"))
      tokens  <- EitherT(realmService.fetchInitialAccessTokens("test_realm"))
      token   <- EitherT.fromOption[IO](tokens.headOption, Errors.NO_TOKENS_FOUND)
    } yield {
      token.count shouldBe 5
      storedTokenId.set(token.id)
    }
  }.value.shouldReturnSuccess

  "deleteInitialAccessTokens" should "delete a specified initial access token" in {
    for {
      _       <- EitherT(realmService.deleteInitialAccessToken(storedTokenId.get(), "test_realm"))
      tokens  <- EitherT(realmService.fetchInitialAccessTokens("test_realm"))
    } yield tokens.map(_.id) shouldNot contain (storedTokenId.get())
  }.value.shouldReturnSuccess

  "clearKeysCache" should "clear a Realm's keys cache" in {
    realmService.clearKeysCache("test_realm").shouldReturnSuccess
  }

  "clearUserCache" should "clear a Realm's user cache" in {
    realmService.clearUserCache("test_realm").shouldReturnSuccess
  }

  "clearRealmCache" should "clear a Realm's realm cache" in {
    realmService.clearRealmCache("test_realm").shouldReturnSuccess
  }

  "delete" should "delete a Realm with a specified name" in {
    for {
      _ <- EitherT(realmService.delete("test_realm"))
      _ <- EitherT(realmService.delete("test_realm2"))
    } yield ()
  }.value.shouldReturnSuccess
}
