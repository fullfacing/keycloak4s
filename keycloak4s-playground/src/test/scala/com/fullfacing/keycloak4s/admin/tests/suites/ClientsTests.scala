package com.fullfacing.keycloak4s.admin.tests.suites

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.data.EitherT
import cats.implicits._
import com.fullfacing.keycloak4s.admin.tests.IntegrationSpec
import com.fullfacing.keycloak4s.core.models._
import monix.eval.Task
import org.scalatest.DoNotDiscover

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
    *
    */

  /* References for storing tests results to be used in subsequent tests. **/
  val storedClients: AtomicReference[Seq[Client]]           = new AtomicReference[Seq[Client]]()
  val storedUsers: AtomicReference[Seq[User]]               = new AtomicReference[Seq[User]]()
  val storedClientScopes: AtomicReference[Seq[ClientScope]] = new AtomicReference[Seq[ClientScope]]()
  val client1: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val client2: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val client3: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val client4: AtomicReference[UUID]                        = new AtomicReference[UUID]()
  val user1: AtomicReference[UUID]                          = new AtomicReference[UUID]()
  val clientScope1: AtomicReference[UUID]                   = new AtomicReference[UUID]()
  val clientScope2: AtomicReference[UUID]                   = new AtomicReference[UUID]()

  "Create Ancillary Objects" should "create all objects needed to test all the clients service calls" in {
    val task =
      for {
        _ <- clientService.create(Client.Create("Client 1"))
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
        _  <- EitherT(clientService.update(client1.get, Client.Update(client1.get, "Client 5")))
        uc <- EitherT(clientService.fetchById(client1.get))
      } yield uc.clientId should equal ("Client 5")).value
    task.shouldReturnSuccess

  }

  "getClientInstallationProvider" should "return the client installation file" in {
    clientService.getClientInstallationProvider(client1.get).shouldReturnSuccess
  }

  "updateClientForServiceAccount" should "update client to enable service accounts." in {
    clientService.update(
      client1.get,
      Client.Update(client1.get, "Client 1", publicClient = Some(false), serviceAccountsEnabled = Some(true))
    ).shouldReturnSuccess
  }

  "fetchServiceAccountUser" should "return a client service account user" in {
    clientService.fetchServiceAccountUser(client1.get).shouldReturnSuccess
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

  "fetchOfflineSessions" should "return a list of offline user sessions associated with this client" in {
    clientService.fetchOfflineSessions(client1.get).shouldReturnSuccess
  }

  "generateNewCertificate" should "return a new certificate with new key pair" in {
    clientService.generateNewCertificate("key1", client2.get).shouldReturnSuccess
  }

  "fetchKeyInfo" should "return the key info" in {
    clientService.fetchKeyInfo("key1", client2.get).shouldReturnSuccess
  }

  "Create Ancillary Objects" should "create all objects needed to test all the clients service scope calls" in {
    val task =
      for {
        _ <- clientService.createClientScope(ClientScope.Create("ClientScope 1"))
        r <- clientService.createClientScope(ClientScope.Create("ClientScope 2"))
      } yield r

    task.shouldReturnSuccess
  }

  "fetch client scope" should "successfully retrieve all client scopes" in {
    clientService.fetchClientScopes().map { response =>
      response.map(scopes => storedClientScopes.set(scopes))
      response shouldBe a [Right[_, _]]
    }.runToFuture
  }

  "Fetch Ancillary Object's UUIDs for ClientScopes" should
    "retrieve the created objects and store their IDs for ClientScopes" in {
    val task = clientService.fetchClientScopes().map(_.map { c =>
      clientScope1.set(c.find(_.name == "ClientScope_1").get.id)
      clientScope2.set(c.find(_.name == "ClientScope_2").get.id)
    })

    task.shouldReturnSuccess
  }

  "fetch client scope by id" should "retrieve a specific client scope" in {
    clientService.fetchClientScopeById(clientScope1.get).shouldReturnSuccess
  }

  "update client scope" should "return the updated client scope" in {
    clientService.updateClientScope(clientScope1.get, ClientScope.Update("ClientScope 3".some)).shouldReturnSuccess
  }

  "fetchDefaultClientScopes" should "return the default client scopes" in {
    clientService.fetchDefaultClientScopes(client3.get).shouldReturnSuccess
  }

  "updateDefaultClientScopes" should "return the updated default client scopes" in {
    clientService.updateDefaultClientScope(client3.get, clientScope1.get).shouldReturnSuccess
  }

  "deleteDefaultClientScope" should "delete a default client scope" in {
    clientService.deleteDefaultClientScope(client3.get, clientScope1.get).shouldReturnSuccess
  }

  "fetchOptionalClientScopes" should "return the optional client scopes" in {
    clientService.fetchOptionalClientScopes(client3.get).shouldReturnSuccess
  }

  "updateOptionalClientScopes" should "return the updated optional client scopes" in {
    clientService.updateOptionalClientScope(client3.get, clientScope2.get).shouldReturnSuccess
  }

  "deleteOptionalClientScope" should "delete a optional client scope" in {
    clientService.deleteOptionalClientScope(client3.get, clientScope2.get).shouldReturnSuccess
  }

  "deleteClientScope" should "delete all client scopes" in {
    val task =
      (for {
        _  <- EitherT(clientService.deleteClientScope(clientScope1.get))
        cs <- EitherT(clientService.deleteClientScope(clientScope2.get))
      }yield cs ).value
    task.shouldReturnSuccess
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
        _ <- clientService.remove(client1.get)
        _ <- clientService.remove(client2.get)
        _ <- clientService.remove(client3.get)
        _ <- clientService.remove(client4.get)
        r <- userService.delete(user1.get)
      } yield r

    task.shouldReturnSuccess
  }

}