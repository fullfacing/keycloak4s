package suites

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.data.{EitherT, Nested}
import cats.implicits._
import com.fullfacing.keycloak4s.core.models._
import monix.eval.Task
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

@DoNotDiscover
class ClientsTests extends IntegrationSpec {

  /**
    * Calls in the Clients service not covered by the below tests:
    *
    * uploadCertificateWithPrivateKey    - Requires setup not feasible for testing.
    * uploadCertificateWithoutPrivateKey - Requires setup not feasible for testing.
    * fetchEffectiveScopeMapping         - Requires setup not feasible for testing.
    * fetchNonScopeRoles                 - Requires setup not feasible for testing.
    * fetchKeyStoreFile                  - Requires data not feasible for testing.
    * generateNewCertificate             - Requires data not feasible for testing.
    * generateAndDownloadNewCertificate  - Requires data not feasible for testing.
    */

  /* References for storing tests results to be used in subsequent tests. **/
  val storedClients: AtomicReference[Seq[Client]]           = new AtomicReference[Seq[Client]]()
  val storedUsers: AtomicReference[Seq[User]]               = new AtomicReference[Seq[User]]()
  val client1: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val client2: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val client3: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val client4: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val client5: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val user1: AtomicReference[UUID]                          = new AtomicReference[UUID]()
  val clientScope1: AtomicReference[UUID]                   = new AtomicReference[UUID]()
  val clientScope2: AtomicReference[UUID]                   = new AtomicReference[UUID]()

  "Create Ancillary Objects" should "create all objects needed to test all the clients service calls" in {
    val task =
      for {
        _ <- clientService.create(Client.Create("Client 1", fullScopeAllowed = false))
        _ <- clientService.create(Client.Create("Client 2"))
        _ <- clientService.create(Client.Create("Client 3"))
        _ <- clientService.create(Client.Create("Client 4"))
        r <- userService.create(User.Create("user1", enabled = true))
      } yield r

    task.shouldReturnSuccess
  }

  "Fetch Ancillary Object's UUIDs" should "retrieve the created objects and store their IDs" in {
    val task: EitherT[Task, KeycloakError, Unit] =
      for {
        c <- EitherT(clientService.fetch())
        u <- EitherT(userService.fetch())
      } yield {
        client1.set(c.find(_.clientId == "Client 1").get.id)
        client2.set(c.find(_.clientId == "Client 2").get.id)
        client3.set(c.find(_.clientId == "Client 3").get.id)
        client4.set(c.find(_.clientId == "Client 4").get.id)
        user1.set(u.find(_.username == "user1").get.id)
      }

    task.value.shouldReturnSuccess
  }

  "createAndRetrieve" should "create a Client and subsequently fetch it" in {
    clientService.createAndRetrieve(Client.Create("Client 5")).map(_.map { client =>
      client5.set(client.id)
    })
  }.shouldReturnSuccess

  "fetchClients" should "successfully retrieve all clients" in {
    clientService.fetch().map { response =>
      response.map(clients => storedClients.set(clients))
      response shouldBe a [Right[_, _]]
    }.runToFuture
  }

  "fetchUsers" should "successfully retrieve all users" in {
    userService.fetch().map { response =>
      response.map(users => storedUsers.set(users))
      response shouldBe a [Right[_, _]]
    }.runToFuture
  }

  "fetchById" should "successfully retrieve a client according to the given client Id" in {
    clientService.fetchById(client1.get).shouldReturnSuccess
  }

  "update" should "update a specific client" in {
    val task =
      (for {
        _  <- EitherT(clientService.update(client1.get, Client.Update(client1.get, "Client 6")))
        uc <- EitherT(clientService.fetchById(client1.get))
      } yield uc.clientId should equal ("Client 6")).value
    task.shouldReturnSuccess
  }

  "getClientInstallationProvider" should "return the client installation file" in {
    clientService.getClientInstallationProvider(client1.get).shouldReturnSuccess
  }

  "updateClientForServiceAccount" should "update client to enable service accounts" in {
    clientService.update(
      client1.get,
      Client.Update(client1.get, "Client 1", publicClient = Some(false), serviceAccountsEnabled = Some(true))
    ).shouldReturnSuccess
  }

  "fetchServiceAccountUser" should "return a client service account user" in {
    clientService.fetchServiceAccountUser(client1.get).map(_.map { account =>
      account.username should equal ("service-account-client 1")
    }).shouldReturnSuccess
  }

  "fetchUserSessionCount & fetchOfflineSessionCount" should
    "return a number of user sessions and offline user sessions associated with the client" in {
    val task =
      (for {
        usc <- EitherT(clientService.fetchUserSessionCount(client1.get))
        osc <- EitherT(clientService.fetchOfflineSessionCount(client1.get))
      } yield {
        usc.count should be (0)
        osc.count should be (0)
      }).value
    task.shouldReturnSuccess
  }

  "fetchUserSessions" should "return a list of user sessions associated with the client" in {
    clientService.fetchUserSessions(client1.get).shouldReturnSuccess
  }

  "fetchUserSessionsS" should "stream a list of user sessions associated with this client" in {
    clientService.fetchUserSessionsS(id = client1.get).completedL.map(_ shouldBe (())).runToFuture
  }

  "fetchOfflineSessions" should "return a list of offline user sessions associated with this client" in {
    clientService.fetchOfflineSessions(client1.get).shouldReturnSuccess
  }

  "fetchOfflineSessionsS" should "stream a list of offline user sessions associated with this client" in {
    clientService.fetchOfflineSessionsS(id = client1.get).completedL.map(_ shouldBe (())).runToFuture
  }

  "generateNewCertificate" should "return a new certificate with new key pair" in {
    clientService.generateNewCertificate("key1", client2.get).shouldReturnSuccess
  }

  "fetchKeyInfo" should "return the key info" in {
    clientService.fetchKeyInfo("key1", client2.get).shouldReturnSuccess
  }

  /* Client Scope(Role) Mapping Tests */

  val realmRole1 = new AtomicReference[UUID]
  val realmRole2 = new AtomicReference[UUID]
  val clientRole1 = new AtomicReference[UUID]
  val clientRole2 = new AtomicReference[UUID]

  "create supporting objects" should "" in {
    val task =
      for {
        _ <- EitherT(realmRoleService.create(Role.Create(name = "RealmRole1", clientRole = false, composite = false)))
        _ <- EitherT(realmRoleService.create(Role.Create(name = "RealmRole2", clientRole = false, composite = false)))
        _ <- EitherT(clientRoleService.create(client2.get(), Role.Create(name = "ClientRole1", clientRole = true, composite = false)))
        _ <- EitherT(clientRoleService.create(client2.get(), Role.Create(name = "ClientRole2", clientRole = true, composite = false)))
        r <- EitherT(realmRoleService.fetch())
        c <- EitherT(clientRoleService.fetch(client2.get()))
        r1 <- EitherT.fromOption[Task](r.find(_.name == "RealmRole1"), Errors.ROLE_NOT_FOUND)
        r2 <- EitherT.fromOption[Task](r.find(_.name == "RealmRole2"), Errors.ROLE_NOT_FOUND)
        c1 <- EitherT.fromOption[Task](c.find(_.name == "ClientRole1"), Errors.ROLE_NOT_FOUND)
        c2 <- EitherT.fromOption[Task](c.find(_.name == "ClientRole2"), Errors.ROLE_NOT_FOUND)
      } yield {
        realmRole1.set(r1.id)
        realmRole2.set(r2.id)
        clientRole1.set(c1.id)
        clientRole2.set(c2.id)
      }

    task.value.shouldReturnSuccess
  }

  "fetchMappedRoles empty" should "return an empty list" in {
    Nested(clientService.fetchMappedRoles(client1.get())).map { r =>
      r.realmMappings.isEmpty  shouldBe true
      r.clientMappings.isEmpty shouldBe true
    }.value.shouldReturnSuccess
  }

  "addRealmRoles" should "map a realm role to a client's scope" in {
    val task =
      for {
        _ <- EitherT(clientService.addRealmRoles(client1.get(), List(realmRole1.get())))
        m <- EitherT(clientService.fetchMappedRoles(client1.get()))
      } yield {
        m.realmMappings.map(_.id) should contain (realmRole1.get())
      }

    task.value.shouldReturnSuccess
  }

  "fetchMappedRealmRoles" should "retrieve all realm roles mapped to the client's scope" in {
    Nested(clientService.fetchMappedRealmRoles(client1.get())).map { r =>
      r.nonEmpty shouldBe true
      r.map(_.id) should contain (realmRole1.get())
      r.map(_.id) should not contain realmRole2.get()

    }.value.shouldReturnSuccess
  }

  "fetchAvailableRealmRoles" should "fetch available realm roles that have not been mapped to the client's scope" in {
    Nested(clientService.fetchAvailableRealmRoles(client1.get())).map { r =>
      r.nonEmpty shouldBe true
      r.map(_.id) should not contain realmRole1.get()
      r.map(_.id) should contain (realmRole2.get())
    }.value.shouldReturnSuccess
  }

  "fetchEffectiveRealmRoles" should "retrieve all mapped realm roles and all their sub roles" in {
    val task =
      for {
        _ <- EitherT(rolesByIdService.addCompositeRoles(realmRole1.get(), List(realmRole2.get(), clientRole2.get())))
        r <- EitherT(clientService.fetchEffectiveRealmRoles(client1.get()))
      } yield {
        r.map(_.id) should contain allOf (realmRole1.get(), realmRole2.get())
      }

    task.value.shouldReturnSuccess
  }

  "removeRealmRoles" should "un-map the specified realm roles from the client's scope" in {
    val task =
      for {
        _ <- EitherT(clientService.removeRealmRoles(client1.get(), List(realmRole1.get())))
        s <- EitherT(clientService.fetchMappedRealmRoles(client1.get()))
      } yield {
        s.map(_.id) should not contain realmRole1.get()
      }

    task.value.shouldReturnSuccess
  }

  "addClientRoles" should "map a client role to a client's scope" in {
    val task =
      for {
        _ <- EitherT(clientService.addClientRoles(client1.get(), client2.get(), List("ClientRole1")))
        m <- EitherT(clientService.fetchMappedRoles(client1.get()))
        c <- EitherT.fromOption[Task](m.clientMappings.find { case (k, _) => k == "Client 2" }, Errors.CLIENT_NOT_FOUND)
      } yield {
        val (_, roles) = c
        roles.mappings.map(_.id) should contain (clientRole1.get())
      }

    task.value.shouldReturnSuccess
  }

  "fetchMappedClientRoles" should
    "retrieve all client roles from the given source client mapped to the client's scope" in {

    Nested(clientService.fetchMappedClientRoles(client1.get(), client2.get())).map { r =>
      r.nonEmpty shouldBe true
      r.map(_.id) should contain (clientRole1.get())
      r.map(_.id) should not contain clientRole2.get()

    }.value.shouldReturnSuccess
  }

  "fetchAvailableClientRoles" should
    "retrieve available client roles from the given source client that can be mapped to the client's scope" in {
    Nested(clientService.fetchAvailableClientRoles(client1.get(), client2.get())).map { r =>
      r.nonEmpty shouldBe true
      r.map(_.id) should not contain clientRole1.get()
      r.map(_.id) should contain (clientRole2.get())
    }.value.shouldReturnSuccess
  }

  "fetchEffectiveClientRoles" should "retrieve all mapped client roles from the given source client and all their sub roles" in {
    val task =
      for {
        _ <- EitherT(rolesByIdService.addCompositeRoles(clientRole1.get(), List(realmRole2.get(), clientRole2.get())))
        r <- EitherT(clientService.fetchEffectiveClientRoles(client1.get(), client2.get()))
      } yield {
        r.map(_.id) should contain allOf (clientRole1.get(), clientRole2.get())
      }

    task.value.shouldReturnSuccess
  }

  "removeClientRoles" should "remove the specified client roles from the client's scope" in {
    val task =
      for {
        _ <- EitherT(clientService.removeClientRoles(client1.get(), client2.get(), List("ClientRole1")))
        s <- EitherT(clientService.fetchMappedClientRoles(client1.get(), client2.get()))
      } yield {
        s.map(_.id) should not contain clientRole1.get()
      }

    task.value.shouldReturnSuccess
  }

  "fetchMappedRoles" should "retrieve all roles mapped to a client's scope" in {
    val task =
      for {
        _ <- EitherT(clientService.addRealmRoles(client1.get(), List(realmRole1.get(), realmRole2.get())))
        _ <- EitherT(clientService.addClientRoles(client1.get(), client2.get(), List("ClientRole1", "ClientRole2")))
        m <- EitherT(clientService.fetchMappedRoles(client1.get()))
        r <- EitherT.fromOption[Task](m.clientMappings.find { case (k, _) => k == "Client 2" }, Errors.CLIENT_NOT_FOUND)
      } yield {
        val (_, c) = r
        m.realmMappings.map(_.id) should contain allOf (realmRole1.get(), realmRole2.get())
        c.mappings.map(_.id) should contain allOf (clientRole1.get(), clientRole2.get())
      }

    task.value.shouldReturnSuccess
  }

  "Create Ancillary Objects" should "create all objects needed to test all the clients service scope calls" in {
    val task =
      for {
        _ <- clientScopeService.create(ClientScope.Create("ClientScope 1"))
        r <- clientScopeService.create(ClientScope.Create("ClientScope 2"))
      } yield r

    task.shouldReturnSuccess
  }

  "Fetch Ancillary Object's UUIDs for ClientScopes" should
    "retrieve the created objects and store their IDs for ClientScopes" in {
    val task = clientScopeService.fetch().map(_.map { c =>
      clientScope1.set(c.find(_.name == "ClientScope_1").get.id)
      clientScope2.set(c.find(_.name == "ClientScope_2").get.id)
    })

    task.shouldReturnSuccess
  }

  "updateDefaultClientScopes" should "return the updated default client scopes" in {
    clientService.updateDefaultClientScope(client3.get, clientScope1.get).shouldReturnSuccess
  }

  "fetchDefaultClientScopes" should "return the default client scopes" in {
    EitherT(clientService.fetchDefaultClientScopes(client3.get))
      .map(_.find(_.name == "ClientScope_3")).value.shouldReturnSuccess
  }

  "deleteDefaultClientScope" should "delete a default client scope" in {
    clientService.deleteDefaultClientScope(client3.get, clientScope1.get).shouldReturnSuccess
  }

  "fetchDefaultScopesAfterDelete" should "return the default client scopes to check if they were deleted" in {
    EitherT(clientService.fetchDefaultClientScopes(client3.get))
      .map(_.find(_.name == "ClientScope_3") should be (empty)).value.shouldReturnSuccess
  }

  "updateOptionalClientScopes" should "return the updated optional client scopes" in {
    clientService.updateOptionalClientScope(client3.get, clientScope2.get).shouldReturnSuccess
  }

  "fetchOptionalClientScopes" should "return the optional client scopes" in {
    EitherT(clientService.fetchOptionalClientScopes(client3.get))
      .map(_.find(_.name == "ClientScope_2")).value.shouldReturnSuccess
  }

  "deleteOptionalClientScope" should "delete a optional client scope" in {
    clientService.deleteOptionalClientScope(client3.get, clientScope2.get).shouldReturnSuccess
  }

  "fetchOptionalScopesAfterDelete" should "return the optional client scopes to check if they were deleted" in {
    EitherT(clientService.fetchOptionalClientScopes(client3.get))
    .map(_.find(_.name == "ClientScope_2") should be (empty)).value.shouldReturnSuccess
  }

  "deleteClientScope" should "delete all client scopes" in {
    val task =
      (for {
        _  <- EitherT(clientScopeService.delete(clientScope1.get))
        cs <- EitherT(clientScopeService.delete(clientScope2.get))
      }yield cs ).value
    task.shouldReturnSuccess
  }

  "fetchClientScopeAfterDelete" should
    "successfully retrieve all client scopes to check if ll of them were deleted" in {
    clientScopeService.fetch().map(_.map { response =>
      response shouldNot contain only ("ClientScope_1", "ClientScope_2")
    }).shouldReturnSuccess
  }

  "ManagementPermissions" should "fetch and update requests for ManagementPermission model" in {
    val task =
      (for {
        cp  <- EitherT(clientService.fetchClientAuthorisationPermissions(client3.get))
        ucp <- EitherT(clientService.updateClientAuthorisationPermissions(client3.get,
          ManagementPermission(enabled = false, cp.resource, cp.scopePermissions)))
      } yield {
        ucp.enabled should equal(false)
      }).value
    task.shouldReturnSuccess
  }

  "registerClusterNode" should "register a cluster node with the client" in {
    clientService.registerClusterNode(client3.get, "127.0.0.1").shouldReturnSuccess
  }

  "unregisterClusterNode" should "unregister a cluster node from the client" in {
    clientService.unregisterClusterNode(client3.get, "127.0.0.1").shouldReturnSuccess
  }

  "testNodesAvailability" should "test availability by sending 'ping' request to all cluster nodes" in {
    clientService.testNodesAvailability(client3.get).shouldReturnSuccess
  }

  "regenerateClientSecret" should "generate a new secret for the client" in {
    clientService.regenerateClientSecret(client4.get).shouldReturnSuccess
  }

  "fetchClientSecret" should "get the client secret" in {
    clientService.fetchClientSecret(client4.get).shouldReturnSuccess
  }

  "regenerateRegistrationAccessToken" should "generate a new registration access token for the client" in {
    clientService.regenerateRegistrationAccessToken(client4.get).shouldReturnSuccess
  }

  "generateAccessTokenExample" should "generate an example access token" in {
    clientService.generateAccessTokenExample(id = client4.get, userId = Some(user1.get.toString)).shouldReturnSuccess
  }

  "fetchProtocolMappers" should
    "return list of all protocol mappers, which will be used when generating tokens issued for particular client" in {
    clientService.fetchProtocolMappers(client4.get).shouldReturnSuccess
  }

  "pushRevocationPolicy" should "push the client’s revocation policy to its admin URL" in {
    clientService.pushRevocationPolicy(client4.get).shouldReturnSuccess
  }

  "Delete Ancillary Objects" should "remove all the ancillary objects created for testing Clients" in {
    val task =
      for {
        _ <- clientService.delete(client1.get)
        _ <- clientService.delete(client2.get)
        _ <- clientService.delete(client3.get)
        _ <- clientService.delete(client4.get)
        _ <- clientService.delete(client5.get)
        _ <- rolesByIdService.delete(realmRole1.get())
        _ <- rolesByIdService.delete(realmRole2.get())
        r <- userService.delete(user1.get)
      } yield r

    task.shouldReturnSuccess
  }

}