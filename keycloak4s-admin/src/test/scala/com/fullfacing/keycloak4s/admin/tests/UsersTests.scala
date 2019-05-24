package com.fullfacing.keycloak4s.admin.tests

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.data.EitherT
import cats.implicits._
import com.fullfacing.keycloak4s.admin.IntegrationSpec
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.models.enums.CredentialTypes
import org.scalatest.DoNotDiscover

@DoNotDiscover
class UsersTests extends IntegrationSpec {

  val storedUsers: AtomicReference[List[User]] = new AtomicReference[List[User]]()
  val storedGroups: AtomicReference[List[Group]] = new AtomicReference[List[Group]]()
  val storedRoleId: AtomicReference[UUID] = new AtomicReference[UUID]()
  val storedClientId: AtomicReference[UUID] = new AtomicReference[UUID]()

  "create" should "successfully create a User" in {
    (for {
      _ <- EitherT(userService.create(User.Create(username = "test_user1", enabled = true)))
      _ <- EitherT(userService.create(User.Create(username = "test_user2", enabled = true)))
      _ <- EitherT(userService.create(User.Create(username = "test_user3", enabled = true)))
      _ <- EitherT(userService.create(User.Create(username = "test_user4", enabled = true)))
      _ <- EitherT(userService.create(User.Create(username = "test_user5", enabled = true)))
    } yield ()).value.map(isSuccessful).unsafeToFuture()
  }

  "fetch" should "successfully retrieve all Users" in {
    userService.fetch().map { response =>
      response.map(users => storedUsers.set(users))
      isSuccessful(response)
    }.unsafeToFuture()
  }

  it should "also be able to retrieve a User by username" in {
    val userToFetch = storedUsers.get().headOption

    EitherT(userService.fetch(username = userToFetch.map(_.username))).map { user =>
      user.headOption shouldNot be(None)
      user.headOption.map(_.id) shouldBe userToFetch.map(_.id)
    }.value.map(isSuccessful).unsafeToFuture()
  }

  "fetchById" should "successfully retrieve an existing User with a given ID" in {
    val userToFetchOpt = storedUsers.get().headOption
    userToFetchOpt shouldNot be(None)
    val userToFetch = userToFetchOpt.get

    EitherT(userService.fetchById(userToFetch.id)).map { user =>
      user.username shouldBe userToFetch.username
    }.value.map(isSuccessful).unsafeToFuture()
  }

  "count" should "correctly return the amount of Users" in {
    userService.count().map(_.map { num =>
      num shouldBe 6
    }).map(isSuccessful).unsafeToFuture()
  }

  "update" should "successfully update an existing User with a given ID" in {
    val userToUpdateOpt = storedUsers.get().find(_.username == "test_user5")
    userToUpdateOpt shouldNot be(None)
    val userToUpdate = userToUpdateOpt.get

    for {
      _     <- EitherT(userService.update(userToUpdate.id, User.Update(enabled = Some(false))))
      user  <- EitherT(userService.fetch(username = Some("test_user5")))
    } yield user.headOption.map(_.id) shouldBe Some(userToUpdate.id)
  }.value.map(isSuccessful).unsafeToFuture()

  "addToGroup" should "successfully add a Group to a User" in {
    val group1 = Group.Create(name = "test_group1")
    val group2 = Group.Create(name = "test_group2")
    val group3 = Group.Create(name = "test_group3")

    val users = storedUsers.get()
    val user1 = users.find(_.username == "test_user1").get.id
    val user2 = users.find(_.username == "test_user2").get.id

    (for {
      _       <- EitherT(groupService.create(group1))
      _       <- EitherT(groupService.create(group2))
      _       <- EitherT(groupService.create(group3))
      groups  <- EitherT(groupService.fetch())
      _       <- EitherT(userService.addToGroup(user1, groups.head.id))
      _       <- EitherT(userService.addToGroup(user2, groups(1).id))
      _       <- EitherT(userService.addToGroup(user2, groups(2).id))
    } yield {
      storedGroups.set(groups.toList)
    }).value.map(isSuccessful).unsafeToFuture()
  }

  "fetchGroups" should "successfully retrieve all Groups a User is added to" in {
    val users = storedUsers.get()
    val user1 = users.find(_.username == "test_user1").get.id
    val user2 = users.find(_.username == "test_user2").get.id

    (for {
      groups1 <- EitherT(userService.fetchGroups(user1))
      groups2 <- EitherT(userService.fetchGroups(user2))
    } yield {
      groups1.map(_.name) should contain only "test_group1"
      groups2.map(_.name) should contain only ("test_group2", "test_group3")
    }).value.map(isSuccessful).unsafeToFuture()
  }

  "countGroups" should "accurately return the amount of Groups a User is added to" in {
    val users = storedUsers.get()
    val user1 = users.find(_.username == "test_user1").get.id
    val user2 = users.find(_.username == "test_user2").get.id

    (for {
      groups1 <- EitherT(userService.countGroups(user1))
      groups2 <- EitherT(userService.countGroups(user2))
    } yield {
      groups1 shouldBe Count(1)
      groups2 shouldBe Count(2)
    }).value.map(isSuccessful).unsafeToFuture()
  }

  "removeFromGroup" should "successfully remove a Group from a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get
    val groups = storedGroups.get()
    val groupId = groups.find(_.name == "test_group1").get.id

    def deleteGroups = groups.map { group =>
      groupService.delete(group.id)
    }.parSequence.map(_.sequence)

    for {
      _   <- EitherT(userService.removeFromGroup(user.id, groupId))
      num <- EitherT(userService.countGroups(user.id))
      _   <- EitherT(deleteGroups)
    } yield num shouldBe Count(0)
  }.value.map(isSuccessful).unsafeToFuture()

  "createFederatedIdentity" should "successfully link a federated identity to a User" in {
    val config = Map(
      "authorizationUrl"  -> "http://localhost",
      "tokenUrl"          -> "http://localhost",
      "clientId"          -> "id",
      "clientSecret"      -> "secret"
    )

    val identityProvider = IdentityProvider(
      config      = config.some,
      alias       = Some("oidc"),
      providerId  = Some("oidc")
    )

    val user = storedUsers.get().find(_.username == "test_user1").get

    for {
      _ <- EitherT(identityProviderService.create(identityProvider))
      _ <- EitherT(userService.createFederatedIdentity(user.id, "oidc", FederatedIdentity(Some("oidc"), Some(user.id.toString), Some(user.username))))
    } yield ()
  }.value.map(isSuccessful).unsafeToFuture()

  "fetchFederatedIdentities" should "successfully retrieve all federated identities linked to a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get

    userService.fetchFederatedIdentities(user.id).map(_.map { fi =>
      fi should contain only FederatedIdentity(Some("oidc"), Some(user.id.toString), Some(user.username))
    }).map(isSuccessful).unsafeToFuture()
  }

  "removeFederatedIdentityProvider" should "successfully unlink a federated identity to a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get

    for {
      _ <- EitherT(userService.removeFederatedIdentityProvider(user.id, "oidc"))
      _ <- EitherT(identityProviderService.delete("oidc"))
    } yield ()
  }.value.map(isSuccessful).unsafeToFuture()

  "fetchRoles" should "successfully retrieve all Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    userService.fetchRoles(user.id).map(_.map { roles =>
      roles.realmMappings shouldNot be (empty)
      roles.clientMappings.get("account") shouldNot be (None)
    }).map(isSuccessful).unsafeToFuture()
  }

  "fetchRealmRoles" should "successfully retrieve all Realm Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    userService.fetchRealmRoles(user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).map(isSuccessful).unsafeToFuture()
  }

  "addRealmRoles" should "successfully map a Realm Role to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    for {
      _   <- EitherT(realmRoleService.create(Role.Create(name = "test_role", clientRole = false, composite = false)))
      id  <- EitherT(realmRoleService.fetchByName("test_role")).map(_.id)
      _   <- EitherT(userService.addRealmRoles(user.id, List(Role.Mapping(Some(id), Some("test_role")))))
    } yield storedRoleId.set(id)
  }.value.map(isSuccessful).unsafeToFuture()

  "removeRealmRoles" should "successfully unmap a Realm Role from a User" in {
    val user = storedUsers.get().find(_.username == "admin").get
    val role = Role.Mapping(name = "test_role".some, id = storedRoleId.get.some)

    for {
      _ <- EitherT(userService.removeRealmRoles(user.id, List(role)))
      _ <- EitherT(realmRoleService.remove("test_role"))
    } yield ()
  }.value.map(isSuccessful).unsafeToFuture()

  "fetchAvailableRealmRoles" should "successfully retrieve all Realm Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get

    userService.fetchAvailableRealmRoles(user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).map(isSuccessful).unsafeToFuture()
  }

  "fetchEffectiveRealmRoles" should "successfully retrieve all Realm Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    userService.fetchEffectiveRealmRoles(user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).map(isSuccessful).unsafeToFuture()
  }

  "fetchClientsRoles" should "successfully retrieve all Client Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    for {
      id    <- EitherT(clientService.fetch(clientId = Some("account"))).map(_.head.id)
      roles <- EitherT(userService.fetchClientRoles(id, user.id))
    } yield {
      roles shouldNot be (empty)
      storedClientId.set(id)
    }
  }.value.map(isSuccessful).unsafeToFuture()

  "addClientRoles" should "successfully map a Client Role to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    for {
      _   <- EitherT(clientRoleService.create(storedClientId.get(), Role.Create(name = "test_role", clientRole = false, composite = false)))
      id  <- EitherT(clientRoleService.fetchByName(storedClientId.get(), "test_role")).map(_.id)
      _   <- EitherT(userService.addClientRoles(storedClientId.get(), user.id, List(Role.Mapping(Some(id), Some("test_role")))))
    } yield storedRoleId.set(id)
  }.value.map(isSuccessful).unsafeToFuture()

  "fetchAvailableClientRoles" should "successfully retrieve all Client Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get

    userService.fetchAvailableClientRoles(storedClientId.get(), user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).map(isSuccessful).unsafeToFuture()
  }

  "removeClientRoles" should "successfully unmap a Client Role from a User" in {
    val user = storedUsers.get().find(_.username == "admin").get
    val role = Role.Mapping(name = "test_role".some, id = storedRoleId.get.some)

    for {
      _ <- EitherT(userService.removeClientRoles(storedClientId.get(), user.id, List(role)))
      _ <- EitherT(clientRoleService.remove(storedClientId.get(), "test_role"))
    } yield ()
  }.value.map(isSuccessful).unsafeToFuture()

  "fetchEffectiveClientRoles" should "successfully retrieve all Client Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    userService.fetchEffectiveClientRoles(storedClientId.get(), user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).map(isSuccessful).unsafeToFuture()
  }

  "fetchSessions" should "successfully retrieve all login sessions for a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    userService.fetchSessions(user.id).map(_.map { sessions =>
      sessions shouldNot be (empty)
    }).map(isSuccessful).unsafeToFuture()
  }

  "fetchOfflineSessions" should "successfully retrieve all offline sessions for a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    userService.fetchOfflineSessions(user.id, storedClientId.get()).map(isSuccessful).unsafeToFuture()
  }

  "removeTotp" should "successfully disable time-based one-time password for a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get

    userService.removeTotp(user.id).map(isSuccessful).unsafeToFuture()
  }

  "resetPassword" should "successfully reset the password for a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get
    val cred = Credential(`type` = CredentialTypes.Password, value = "test_pass")

    userService.resetPassword(user.id, cred).map(isSuccessful).unsafeToFuture()
  }

  "disableUserCredentials" should "successfully disable the specified credential types for a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get

    userService.disableUserCredentials(user.id, List("password")).map(isSuccessful).unsafeToFuture()
  }

  "delete" should "successfully delete an existing User with a given ID" in {
    val ids = storedUsers.get().collect { case u if u.username != "admin" => u.id }

    val ioResults = ids.map { id =>
      userService.delete(id)
    }.parSequence

    ioResults.map { results =>
      forAll(results)(isSuccessful)
    }.unsafeToFuture()
  }

  "logout" should "successfully log out a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    userService.logout(user.id).map(isSuccessful).unsafeToFuture()
  }
}