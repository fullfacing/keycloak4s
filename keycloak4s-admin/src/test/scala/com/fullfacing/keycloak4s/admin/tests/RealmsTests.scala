package com.fullfacing.keycloak4s.admin.tests

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.data.EitherT
import cats.effect.IO
import com.fullfacing.keycloak4s.admin.IntegrationSpec
import com.fullfacing.keycloak4s.core.models.{Group, KeycloakThrowable, RealmEventsConfig, RealmRepresentation}
import com.fullfacing.keycloak4s.core.models.enums.EventTypes
import org.scalatest.DoNotDiscover

@DoNotDiscover
class RealmsTests extends IntegrationSpec {

  /**
   * Calls in the Realms service not covered by the below tests:
   *
   * logoutAll                  - The call seems to be broken on Keycloak's side.
   * clientDescriptionConverter - The exact purpose or use cases for the call is unknown.
   * testLdapConnection         - Requires setup not feasible for testing.
   * testSmtpConnection         - Requires setup not feasible for testing.
   */

  val storedScopeId: AtomicReference[UUID] = new AtomicReference[UUID]()
  val storedGroup: AtomicReference[Group] = new AtomicReference[Group]()

  "create" should "successfully create a new Realm" in {
    val realm = RealmRepresentation.Create(
      id                        = "test_realm",
      realm                     = "test_realm",
      eventsEnabled             = Some(true),
      eventsListeners           = Some(List("jboss-logging")),
      adminEventsEnabled        = Some(true),
      adminEventsDetailsEnabled = Some(true),
      enabledEventTypes         = Some(EventTypes.values.toList)
    )

    realmService.create(realm).shouldReturnSuccess
  }

  "fetch" should "retrieve the Realm as configured in KeycloakClient" in {
    realmService.fetch().map(_.map { realm =>
      realm.id shouldBe "master"
    }).shouldReturnSuccess
  }

  it should "also be able to retrieve a Realm with a specific name" in {
    realmService.fetch("test_realm").map(_.map { realm =>
      realm.id shouldBe "test_realm"
    }).shouldReturnSuccess
  }

  "fetchAll" should "retrieve all Realms" in {
    realmService.fetchAll().map(_.map { realms =>
      realms.map(_.id) should contain only ("master", "test_realm")
    }).shouldReturnSuccess
  }

  "update" should "update a Realm" in {
    val update = RealmRepresentation.Update(enabled = Some(true))
    realmService.update(update, "test_realm").shouldReturnSuccess
  }

  "fetchAdminEvents" should "retrieve all admin events that have been logged" in {
    realmService.fetchAdminEvents(realm = "test_realm").map(_.map { events =>
      events shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "deleteAdminEvents" should "delete all admin events that have been logged" in {
    realmService.deleteAdminEvents(realm = "test_realm").shouldReturnSuccess
  }

  "fetchClientSessionStats" should "retrieve statistics of all active session for a Realm" in {
    realmService.fetchClientSessionStats("master").map(_.map { stats =>
      stats shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "fetchDefaultClientScopes" should "retrieve all assigned default client scopes for a Realm" in {
    realmService.fetchDefaultClientScopes("test_realm").map(_.map { scopes =>
      scopes shouldNot be (empty)
      storedScopeId.set(scopes.head.id)
    }).shouldReturnSuccess
  }

  "unassignDefaultClientScope" should "unassign a default scope to a Realm" in {
    realmService.unassignClientScopeAsDefault(storedScopeId.get(), "test_realm").shouldReturnSuccess
  }

  "assignDefaultClientScope" should "assign a default scope to a Realm" in {
    realmService.assignClientScopeAsDefault(storedScopeId.get(), "test_realm").shouldReturnSuccess
  }

  "assignGroupAsDefault" should "assign a specified group as default in a Realm" in {
    for {
      _     <- EitherT(groupService.create(Group.Create(name = "test_group")))
      opt   <- EitherT(groupService.fetch()).map(_.find(_.name == "test_group"))
      group <- EitherT.fromOption[IO](opt, KeycloakThrowable(new Throwable("No groups found.")))
      _     <- EitherT(realmService.assignGroupAsDefault(group.id))
    } yield storedGroup.set(group)
  }.value.shouldReturnSuccess

  "fetchDefaultGroups" should "retrieve all default groups for a Realm" in {
    val groupStub = Group(
      id    = storedGroup.get().id,
      name  = "test_group",
      path  = "/test_group"
    )

    realmService.fetchDefaultGroups().map(_.map { groups =>
      groups should contain (groupStub)
    }).shouldReturnSuccess
  }

  "fetchGroupByPath" should "retrieve a Group in a Realm by its path" in {
    realmService.fetchGroupByPath(storedGroup.get().path).map(_.map { group =>
      group.id shouldBe storedGroup.get().id
    }).shouldReturnSuccess
  }

  "unassignGroupAsDefault" should "unassign a specified group as default in a Realm" in {
    for {
      _ <- EitherT(realmService.unassignGroupAsDefault(storedGroup.get().id))
      _ <- EitherT(groupService.delete(storedGroup.get().id))
    } yield ()
  }.value.shouldReturnSuccess

  "fetchOptionalClientScopes" should "retrieve all assigned optional client scopes for a Realm" in {
    realmService.fetchOptionalClientScopes("test_realm").map(_.map { scopes =>
      scopes shouldNot be (empty)
      storedScopeId.set(scopes.head.id)
    }).shouldReturnSuccess
  }

  "unassignOptionalClientScope" should "unassign a optional scope to a Realm" in {
    realmService.unassignClientScopeAsOptional(storedScopeId.get(), "test_realm").shouldReturnSuccess
  }

  "assignOptionalClientScope" should "assign a optional scope to a Realm" in {
    realmService.assignClientScopeAsOptional(storedScopeId.get(), "test_realm").shouldReturnSuccess
  }

  "fetchEvents" should "retrieve all logging events for a Realm" in {
    realmService.fetchEvents().map(_.map { events =>
      events shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "deleteAllEvents" should "delete all logging events for a Realm" in {
    realmService.deleteAllEvents().shouldReturnSuccess
  }

  "fetchEventsConfig" should "retrieve the events configuration for a Realm" in {
    realmService.fetchEventsConfig().shouldReturnSuccess
  }

  "updateEventsConfig" should "update the events configuration for a Realm" in {
    val update = RealmEventsConfig.Update(
      adminEventsEnabled  = Some(true),
      eventsEnabled       = Some(true)
    )

    realmService.updateEventsConfig(update).shouldReturnSuccess
  }

  "clearKeysCache" should "clear a Realm's keys cache" in {
    realmService.clearKeysCache("test_realm").shouldReturnSuccess
  }

  "clearUserCache" should "clear a Realm's user cache" in {
    realmService.clearUserCache("test_realm").shouldReturnSuccess
  }

  "clearRealmCache" should "clear a Realm's realm cache" in {
    realmService.clearRealmCache("test_realm").shouldReturnSuccess
  }

  "logoutAll" should "clear all login sessions for a Realm" in {
    realmService.logoutAll("test_realm").shouldReturnSuccess
  }

  "delete" should "delete a Realm with a specified name" in {
    realmService.delete("test_realm").shouldReturnSuccess
  }
}
