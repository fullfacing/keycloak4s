package suites

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.data.EitherT
import cats.implicits._
import utils.{Errors, IntegrationSpec}
import com.fullfacing.keycloak4s.core.models._
import com.fullfacing.keycloak4s.core.models.enums.CredentialTypes
import monix.eval.Task
import org.scalatest.DoNotDiscover

@DoNotDiscover
class UsersTests extends IntegrationSpec {

  /**
   * Calls in the Users service not covered by the below tests:
   *
   * sendVerificationEmail  - Requires setup not feasible for testing.
   * sendActionsEmail       - Requires setup not feasible for testing.
   * impersonate            - Changes permissions, causing interference with subsequent tests.
   */

  /* References for storing tests results to be used in subsequent tests. **/
  val storedUsers: AtomicReference[List[User]]    = new AtomicReference[List[User]]()
  val storedGroups: AtomicReference[List[Group]]  = new AtomicReference[List[Group]]()
  val storedRoleId: AtomicReference[UUID]         = new AtomicReference[UUID]()
  val storedClientId: AtomicReference[UUID]       = new AtomicReference[UUID]()
  val storedAdminUser: AtomicReference[User]      = new AtomicReference[User]()
  val storedTestUser1: AtomicReference[User]      = new AtomicReference[User]()
  val storedTestUser2: AtomicReference[User]      = new AtomicReference[User]()
  val storedTestUser3: AtomicReference[User]      = new AtomicReference[User]()

  "fetch" should "retrieve a list of Users with at least one User" in {
    userService.fetch().map(_.map { response =>
      response.map(_.username) should contain ("admin")
    })
  }.shouldReturnSuccess

  it should "also be able to retrieve a User by username" in {
    userService.fetch(username = Some("admin")).map(_.map { users =>
      users.find(_.username == "admin") shouldNot be(None)
    })
  }.shouldReturnSuccess

  "fetchS" should "be able to stream Users" in {
    userService.fetchS().toListL.map { users =>
      users.map(_.username) should contain ("admin")
    }
  }.runToFuture

  "fetchL" should "be able to stream a sequence of Users" in {
    userService.fetchL().map { users =>
      users.map(_.username) should contain ("admin")
    }
  }.runToFuture

  "create" should "create a User" in {
    for {
      _     <- EitherT(userService.create(User.Create(username = "test_user1", enabled = true)))
      _     <- EitherT(userService.create(User.Create(username = "test_user2", enabled = true)))
      _     <- EitherT(userService.create(User.Create(username = "test_user3", enabled = true)))
      users <- EitherT(userService.fetch())
    } yield {
      users.map(_.username) should contain allOf ("test_user1", "test_user2", "test_user3")
      storedUsers.set(users)
    }
  }.value.shouldReturnSuccess

  "fetchById" should "retrieve an existing User with a specified ID" in {
    val option = storedUsers.get().headOption

    for {
      storedUser  <- EitherT.fromOption[Task](option, Errors.NO_USERS_FOUND)
      fetchedUser <- EitherT(userService.fetchById(storedUser.id))
    } yield fetchedUser.username shouldBe storedUser.username
  }.value.shouldReturnSuccess

  "count" should "correctly return the amount of Users" in {
    userService.count().map(_.map { num =>
      num shouldBe 4
    }).shouldReturnSuccess
  }

  "update" should "update an existing User with a given ID" in {
    val option = storedUsers.get().find(_.username == "test_user3")

    for {
      storedUser  <- EitherT.fromOption[Task](option, Errors.NO_USERS_FOUND)
      _           <- EitherT(userService.update(storedUser.id, User.Update(enabled = Some(false))))
      fetchedUser <- EitherT(userService.fetch(username = Some("test_user3")))
    } yield {
      storedTestUser3.set(storedUser)
      fetchedUser.headOption.map(_.id) shouldBe Some(storedUser.id)
    }
  }.value.shouldReturnSuccess

  "addToGroup" should "add a Group to a User" in {
    val group1 = Group.Create(name = "test_group1")
    val group2 = Group.Create(name = "test_group2")
    val group3 = Group.Create(name = "test_group3")

    val users = storedUsers.get()
    val option1 = users.find(_.username == "test_user1")
    val option2 = users.find(_.username == "test_user2")

    (for {
      user1   <- EitherT.fromOption[Task](option1, Errors.NO_USERS_FOUND)
      user2   <- EitherT.fromOption[Task](option2, Errors.NO_USERS_FOUND)
      _       <- EitherT(groupService.create(group1))
      _       <- EitherT(groupService.create(group2))
      _       <- EitherT(groupService.create(group3))
      groups  <- EitherT(groupService.fetch())
      group1  <- EitherT.fromOption[Task](groups.headOption, Errors.NO_GROUPS_FOUND)
      group2  <- EitherT.fromOption[Task](groups.toList.get(1), Errors.GROUP_NOT_FOUND)
      group3  <- EitherT.fromOption[Task](groups.toList.get(2), Errors.GROUP_NOT_FOUND)
      _       <- EitherT(userService.addToGroup(user1.id, group1.id))
      _       <- EitherT(userService.addToGroup(user2.id, group2.id))
      _       <- EitherT(userService.addToGroup(user2.id, group3.id))
    } yield {
      storedTestUser1.set(user1)
      storedTestUser2.set(user2)
      storedGroups.set(groups.toList)
    }).value.shouldReturnSuccess
  }

  "fetchGroups" should "retrieve a non-empty list of Groups a User is linked to" in {
    val user1 = storedTestUser1.get().id
    val user2 = storedTestUser2.get().id

    for {
      groups1 <- EitherT(userService.fetchGroups(user1))
      groups2 <- EitherT(userService.fetchGroups(user2))
    } yield {
      groups1.map(_.name) should contain only "test_group1"
      groups2.map(_.name) should contain only ("test_group2", "test_group3")
    }
  }.value.shouldReturnSuccess

  "fetchGroupsS" should "be able to stream Groups a User is linked to" in {
    val user1 = storedTestUser1.get().id
    val user2 = storedTestUser2.get().id

    for {
      groups1 <- EitherT.right[KeycloakError](userService.fetchGroupsS(user1).toListL)
      groups2 <- EitherT.right[KeycloakError](userService.fetchGroupsS(user2).toListL)
    } yield {
      groups1.map(_.name) should contain only "test_group1"
      groups2.map(_.name) should contain only ("test_group2", "test_group3")
    }
  }.value.shouldReturnSuccess

  "fetchGroupsL" should "be able to stream a sequence of Groups a User is linked to" in {
    val user1 = storedTestUser1.get().id
    val user2 = storedTestUser2.get().id

    for {
      groups1 <- EitherT.right[KeycloakError](userService.fetchGroupsL(user1))
      groups2 <- EitherT.right[KeycloakError](userService.fetchGroupsL(user2))
    } yield {
      groups1.map(_.name) should contain only "test_group1"
      groups2.map(_.name) should contain only ("test_group2", "test_group3")
    }
  }.value.shouldReturnSuccess

  "countGroups" should "correctly return the amount of Groups a User is added to" in {
    val user1 = storedTestUser1.get().id
    val user2 = storedTestUser2.get().id

    (for {
      groups1 <- EitherT(userService.countGroups(user1))
      groups2 <- EitherT(userService.countGroups(user2))
    } yield {
      groups1 shouldBe Count(1)
      groups2 shouldBe Count(2)
    }).value.shouldReturnSuccess
  }

  "removeFromGroup" should "remove a Group from a User" in {
    val user = storedTestUser1.get()
    val groups = storedGroups.get()
    val option = groups.find(_.name == "test_group1").map(_.id)

    def deleteGroups = groups.map { group =>
      groupService.delete(group.id)
    }.parSequence.map(_.sequence)

    for {
      groupId <- EitherT.fromOption[Task](option, Errors.NO_GROUPS_FOUND)
      _       <- EitherT(userService.removeFromGroup(user.id, groupId))
      num     <- EitherT(userService.countGroups(user.id))
      _       <- EitherT(deleteGroups)
    } yield num shouldBe Count(0)
  }.value.shouldReturnSuccess

  "createFederatedIdentity" should "link a federated identity to a User" in {
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

    val user = storedTestUser1.get()

    for {
      _     <- EitherT(idProvService.create(identityProvider))
      _     <- EitherT(userService.createFederatedIdentity(user.id, "oidc", FederatedIdentity(Some("oidc"), Some(user.id.toString), Some(user.username))))
    } yield ()
  }.value.shouldReturnSuccess

  "fetchFederatedIdentities" should "retrieve a non-empty list of federated identities linked to a User" in {
    val user = storedTestUser1.get()

    EitherT(userService.fetchFederatedIdentities(user.id)).map { identities =>
      val identity = FederatedIdentity(Some("oidc"), Some(user.id.toString), Some(user.username))
      identities should contain only identity
    }
  }.value.shouldReturnSuccess

  "removeFederatedIdentityProvider" should "unlink a federated identity from a User" in {
    val user = storedTestUser1.get()

    for {
      _     <- EitherT(userService.removeFederatedIdentityProvider(user.id, "oidc"))
      _     <- EitherT(idProvService.delete("oidc"))
    } yield ()
  }.value.shouldReturnSuccess

  "fetchRoles" should "retrieve a non-empty list of Roles mapped to a User" in {
    val option = storedUsers.get().find(_.username == "admin")

    for {
      user  <- EitherT.fromOption[Task](option, Errors.NO_USERS_FOUND)
      roles <- EitherT(userService.fetchRoles(user.id))
    } yield {
      storedAdminUser.set(user)
      roles.realmMappings shouldNot be (empty)
      roles.clientMappings.get("account") shouldNot be (None)
    }
  }.value.shouldReturnSuccess

  "fetchRealmRoles" should "retrieve a non-empty list of Realm Roles mapped to a User" in {
    val user = storedAdminUser.get()

    EitherT(userService.fetchRealmRoles(user.id)).map { roles =>
      roles shouldNot be (empty)
    }
  }.value.shouldReturnSuccess

  "addRealmRoles" should "map a Realm Role to a User" in {
    val user = storedAdminUser.get()

    for {
      _     <- EitherT(realmRoleService.create(Role.Create(name = "test_role", clientRole = false, composite = false)))
      id    <- EitherT(realmRoleService.fetchByName("test_role")).map(_.id)
      _     <- EitherT(userService.addRealmRoles(user.id, List(Role.Mapping(Some(id), Some("test_role")))))
      roles <- EitherT(userService.fetchRealmRoles(user.id))
    } yield {
      roles.map(_.name) should contain ("test_role")
      storedRoleId.set(id)
    }
  }.value.shouldReturnSuccess

  "removeRealmRoles" should "unmap a Realm Role from a User" in {
    val user = storedAdminUser.get()
    val role = Role.Mapping(name = "test_role".some, id = storedRoleId.get.some)

    for {
      _     <- EitherT(userService.removeRealmRoles(user.id, List(role)))
      _     <- EitherT(realmRoleService.remove("test_role"))
      roles <- EitherT(userService.fetchRealmRoles(user.id))
    } yield roles.map(_.name) shouldNot contain ("test_role")
  }.value.shouldReturnSuccess

  "fetchAvailableRealmRoles" should "retrieve a non-empty list of Realm Roles mapped to a User" in {
    val user = storedTestUser1.get()

    EitherT(userService.fetchAvailableRealmRoles(user.id)).map { roles =>
      roles shouldNot be (empty)
    }
  }.value.shouldReturnSuccess

  "fetchEffectiveRealmRoles" should "retrieve a non-empty list of Realm Roles mapped to a User" in {
    val user = storedAdminUser.get()

    EitherT(userService.fetchEffectiveRealmRoles(user.id)).map { roles =>
      roles shouldNot be (empty)
    }
  }.value.shouldReturnSuccess

  "fetchClientsRoles" should "retrieve a non-empty list of Client Roles mapped to a User" in {
    val user = storedAdminUser.get()

    for {
      idOpt <- EitherT(clientService.fetch(clientId = Some("account"))).map(_.headOption.map(_.id))
      id    <- EitherT.fromOption[Task](idOpt, Errors.NO_CLIENTS_FOUND)
      roles <- EitherT(userService.fetchClientRoles(id, user.id))
    } yield {
      roles shouldNot be (empty)
      storedClientId.set(id)
    }
  }.value.shouldReturnSuccess

  "fetchAvailableClientRoles" should "retrieve a non-empty list of Client Roles mapped to a User" in {
    val user = storedTestUser1.get()

    for {
      _     <- EitherT(clientRoleService.create(storedClientId.get(), Role.Create(name = "test_role", clientRole = true, composite = false)))
      id    <- EitherT(clientRoleService.fetchByName(storedClientId.get(), "test_role")).map(_.id)
      roles <- EitherT(userService.fetchAvailableClientRoles(storedClientId.get(), user.id))
    } yield {
      roles shouldNot be (empty)
      storedRoleId.set(id)
    }
  }.value.shouldReturnSuccess

  "addClientRoles" should "map a Client Role to a User" in {
    val user = storedAdminUser.get()

    for {
      _     <- EitherT(userService.addClientRoles(storedClientId.get(), user.id, List(Role.Mapping(Some(storedRoleId.get()), Some("test_role")))))
      roles <- EitherT(userService.fetchAvailableClientRoles(storedClientId.get(), user.id))
    } yield roles shouldBe empty
  }.value.shouldReturnSuccess

  "removeClientRoles" should "unmap a Client Role from a User" in {
    val user = storedAdminUser.get()
    val role = Role.Mapping(name = "test_role".some, id = storedRoleId.get.some)

    for {
      _     <- EitherT(userService.removeClientRoles(storedClientId.get(), user.id, List(role)))
      roles <- EitherT(userService.fetchAvailableClientRoles(storedClientId.get(), user.id))
      _     <- EitherT(clientRoleService.remove(storedClientId.get(), "test_role"))
    } yield roles shouldNot be (empty)
  }.value.shouldReturnSuccess

  "fetchEffectiveClientRoles" should "retrieve a non-empty list of Client Roles mapped to a User" in {
    val user = storedAdminUser.get()

    EitherT(userService.fetchEffectiveClientRoles(storedClientId.get(), user.id)).map { roles =>
      roles shouldNot be (empty)
    }
  }.value.shouldReturnSuccess

  "fetchSessions" should "retrieve a non-empty list of login sessions for a User" in {
    val user = storedAdminUser.get()

    EitherT(userService.fetchSessions(user.id))
  }.value.shouldReturnSuccess

  "fetchOfflineSessions" should "retrieve a non-empty list of offline sessions for a User" in {
    val user = storedAdminUser.get()

    for {
      _ <- EitherT(userService.fetchOfflineSessions(user.id, storedClientId.get()))
      _ <- EitherT(userService.fetchSessions(user.id))
    } yield ()
  }.value.shouldReturnSuccess

  "removeTotp" should "disable time-based one-time password for a User" in {
    userService.removeTotp(storedTestUser1.get().id)
  }.shouldReturnSuccess

  "resetPassword" should "reset the password for a User" in {
    val cred = Credential(`type` = CredentialTypes.Password, value = "test_pass")
    userService.resetPassword(storedTestUser1.get().id, cred)
  }.shouldReturnSuccess

  "disableUserCredentials" should "disable the specified credential types for a User" in {
    userService.disableUserCredentials(storedTestUser1.get().id, List("password"))
  }.shouldReturnSuccess

  "logout" should "log out a User" in {
    userService.logout(storedTestUser1.get().id)
  }.shouldReturnSuccess

  "delete" should "delete an existing User with a specified ID" in {
    val ids = storedUsers.get().collect { case u if u.username != "admin" => u.id }

    val ioResults = ids.map { id =>
      userService.delete(id)
    }.parSequence

    for {
      _     <- EitherT(ioResults.map(_.sequence))
      users <- EitherT(userService.fetch())
    } yield users.map(_.username) should contain only "admin"
  }.value.shouldReturnSuccess
}