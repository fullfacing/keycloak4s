package suites

import cats.data.{EitherT, Nested}
import cats.effect.IO
import cats.implicits._
import com.fullfacing.keycloak4s.core.models._
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class ClientScopeTests extends IntegrationSpec {

  private val rRole1Name = "realmRole1"
  val rRole1Create: Role.Create = Role.Create(
    clientRole = false,
    composite  = false,
    name       = rRole1Name
  )

  private val rRole2Name = "realmRole2"
  val rRole2Create: Role.Create = Role.Create(
    clientRole = false,
    composite  = false,
    name       = rRole2Name
  )

  private val cRole1Name = "clientRole1"
  val cRole1Create: Role.Create = Role.Create(
    clientRole = true,
    composite  = false,
    name       = cRole1Name
  )

  private val cRole2Name = "clientRole2"
  val cRole2Create: Role.Create = Role.Create(
    clientRole = true,
    composite  = false,
    name       = cRole2Name
  )

  val clientCreate = Client.Create("scope-mappings-test")

  val scope1Create: ClientScope.Create = ClientScope.Create(
    name = "scope1"
  )

  val scope2Create: ClientScope.Create = ClientScope.Create(
    name = "scope2"
  )

  private val rRole1     = new AtomicReference[UUID]()
  private val rRole2     = new AtomicReference[UUID]()
  private val cRole1     = new AtomicReference[UUID]()
  private val cRole2     = new AtomicReference[UUID]()
  private val clientUuid = new AtomicReference[UUID]()
  private val scope1     = new AtomicReference[UUID]()
  private val scope2     = new AtomicReference[UUID]()


  "Create Ancillary Objects" should "create all objects needed to test all the ClientScope service calls" in {
    val IO =
      for {
        _ <- EitherT(clientService.create(Client.Create("scope-mappings-test")))
        c <- EitherT(clientService.fetch(clientId = Some("scope-mappings-test")))
        c1 <- EitherT.fromOption[IO](c.headOption, Errors.CLIENT_NOT_FOUND)
        _ =  clientUuid.set(c1.id)
        _ <- EitherT(realmRoleService.create(rRole1Create))
        _ <- EitherT(realmRoleService.create(rRole2Create))
        _ <- EitherT(clientRoleService.create(clientUuid.get(), cRole1Create))
        r <- EitherT(clientRoleService.create(clientUuid.get(), cRole2Create))
      } yield r

    IO.value.shouldReturnSuccess
  }

  "Fetch Ancillary Object's UUIDs" should "retrieve the created objects and store their IDs" in {
    val IO: EitherT[IO, KeycloakError, Unit] =
      for {
        rr  <- EitherT(realmRoleService.fetch())
        cr  <- EitherT(clientRoleService.fetch(clientUuid.get()))

        r1  <- EitherT.fromOption[IO](rr.find(_.name == rRole1Name), Errors.ROLE_NOT_FOUND)
        r2  <- EitherT.fromOption[IO](rr.find(_.name == rRole2Name), Errors.ROLE_NOT_FOUND)
        c1  <- EitherT.fromOption[IO](cr.find(_.name == cRole1Name), Errors.ROLE_NOT_FOUND)
        c2  <- EitherT.fromOption[IO](cr.find(_.name == cRole2Name), Errors.ROLE_NOT_FOUND)

      } yield {
        rRole1.set(r1.id)
        rRole2.set(r2.id)
        cRole1.set(c1.id)
        cRole2.set(c2.id)
      }

    IO.value.shouldReturnSuccess
  }

  "create" should "successfully create new client-scope objects" in {
    val IO =
      for {
        _  <- EitherT(clientScopeService.create(scope1Create))
        _  <- EitherT(clientScopeService.create(scope2Create))
        s  <- EitherT(clientScopeService.fetch())
        s1 <- EitherT.fromOption[IO](s.find(_.name == "scope1"), Errors.SCOPE_NOT_FOUND)
        s2 <- EitherT.fromOption[IO](s.find(_.name == "scope2"), Errors.SCOPE_NOT_FOUND)
      } yield {
        scope1.set(s1.id)
        scope2.set(s2.id)
      }

    IO.value.shouldReturnSuccess
  }

  "fetch" should "retrieve all client-scopes matching the query" in {
    Nested(clientScopeService.fetch()).map { s =>
      s.map(_.name) should contain allOf ("scope1", "scope2")
    }.value.shouldReturnSuccess
  }

  "fetchById" should "retrieve the client-scope object matching the given ID" in {
    Nested(clientScopeService.fetchById(scope1.get())).map { s =>
      s.name shouldBe "scope1"
    }.value.shouldReturnSuccess
  }

  "update" should "" in {
    val update = ClientScope.Update(name = Some("scope2Update"), description = Some("Description"))
    val IO =
      for {
        _ <- EitherT(clientScopeService.update(scope2.get(), update))
        s <- EitherT(clientScopeService.fetchById(scope2.get()))
      } yield {
        s.name shouldBe "scope2Update"
        s.description shouldBe Some("Description")
      }

    IO.value.shouldReturnSuccess
  }

  "fetchMappedRoles" should "not return any role mappings" in {
    val IO =
      for {
        s1 <- EitherT(clientScopeService.fetchMappedRoles(scope1.get()))
        s2 <- EitherT(clientScopeService.fetchMappedRoles(scope2.get()))
      } yield {
        s1.clientMappings shouldBe Map.empty[String, ClientMappings]
        s1.realmMappings  shouldBe List.empty[Role]
        s2.clientMappings shouldBe Map.empty[String, ClientMappings]
        s2.realmMappings  shouldBe List.empty[Role]
      }

    IO.value.shouldReturnSuccess
  }

  "addClientRoles" should "map a scope to a client role" in {
    clientScopeService.addClientRoles(scope1.get(), clientUuid.get(), List(cRole1Name))
      .shouldReturnSuccess
  }

  "fetchMappedClientRoles" should "retrieve all client roles mapped to this scope" in {
    val IO =
      EitherT(clientScopeService.fetchMappedClientRoles(scope1.get(), clientUuid.get())).map { s =>
        s.size                   shouldBe 1
        s.headOption.map(_.name) shouldBe Some(cRole1Name)
      }

    IO.value.shouldReturnSuccess
  }

  "fetchAvailableClientRoles" should "retrieve all client roles to which this scope can be mapped" in {
    val IO =
      EitherT(clientScopeService.fetchAvailableClientRoles(scope1.get(), clientUuid.get())).map { s =>
        s.nonEmpty shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  "fetchEffectiveClientRoles" should "retrieve all client roles, along with their sub roles, mapped to this scope" in {
    val IO =
      for {
        cb <- EitherT(clientScopeService.fetchEffectiveClientRoles(scope1.get(), clientUuid.get()))
        rb <- EitherT(clientScopeService.fetchEffectiveRealmRoles(scope1.get()))
        _ <- EitherT(clientRoleService.addCompositeRoles(clientUuid.get(), cRole1Name, List(cRole2.get(), rRole2.get())))
        ca <- EitherT(clientScopeService.fetchEffectiveClientRoles(scope1.get(), clientUuid.get()))
        ra <- EitherT(clientScopeService.fetchEffectiveRealmRoles(scope1.get()))
        _ <- EitherT(clientRoleService.removeCompositeRoles(clientUuid.get(), cRole1Name, List(cRole2.get(), rRole2.get())))
      } yield {
        cb.exists(_.name == cRole1Name) shouldBe true
        cb.exists(_.name == cRole2Name) shouldBe false
        rb.exists(_.name == rRole2Name) shouldBe false
        ca.exists(_.name == cRole1Name) shouldBe true
        ca.exists(_.name == cRole2Name) shouldBe true
        ra.exists(_.name == rRole2Name) shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  "removeClientRoles" should "remove all client role mappings from this scope" in {
    val IO =
      for {
        _ <- EitherT(clientScopeService.removeClientRoles(scope1.get(), clientUuid.get(), List(cRole1Name)))
        s <- EitherT(clientScopeService.fetchMappedRoles(scope1.get()))
      } yield {
        s.clientMappings.isEmpty shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  "addRealmRoles" should "map a scope to a client role" in {
    clientScopeService.addRealmRoles(scope2.get(), List(rRole1.get()))
      .shouldReturnSuccess
  }

  "fetchRealmRoles" should "retrieve all client roles mapped to this scope" in {
    val IO =
      EitherT(clientScopeService.fetchMappedRealmRoles(scope2.get())).map { s =>
        s.size                   shouldBe 1
        s.headOption.map(_.name) shouldBe Some(rRole1Name)
      }

    IO.value.shouldReturnSuccess
  }

  "fetchAvailableRealmRoles" should "retrieve all client roles to which this scope can be mapped" in {
    val IO =
      EitherT(clientScopeService.fetchAvailableRealmRoles(scope2.get())).map { s =>
        s.nonEmpty shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  "fetchEffectiveRealmRoles" should "retrieve all client roles, along with their sub roles, mapped to this scope" in {
    val IO =
      for {
        rb <- EitherT(clientScopeService.fetchEffectiveRealmRoles(scope2.get()))
        cb <- EitherT(clientScopeService.fetchEffectiveClientRoles(scope2.get(), clientUuid.get()))
        _ <- EitherT(realmRoleService.addCompositeRoles(rRole1Name, List(cRole2.get(), rRole2.get())))
        ra <- EitherT(clientScopeService.fetchEffectiveRealmRoles(scope2.get()))
        ca <- EitherT(clientScopeService.fetchEffectiveClientRoles(scope2.get(), clientUuid.get()))
        _ <- EitherT(realmRoleService.removeCompositeRoles(rRole1Name, List(cRole2.get(), rRole2.get())))
      } yield {
        rb.exists(_.name == rRole1Name) shouldBe true
        cb.exists(_.name == cRole2Name) shouldBe false
        rb.exists(_.name == rRole2Name) shouldBe false
        ra.exists(_.name == rRole1Name) shouldBe true
        ca.exists(_.name == cRole2Name) shouldBe true
        ra.exists(_.name == rRole2Name) shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  "removeRealmRoles" should "remove all client role mappings from this scope" in {
    val IO =
      for {
        _ <- EitherT(clientScopeService.removeRealmRoles(scope1.get(), List(rRole1.get())))
        s <- EitherT(clientScopeService.fetchMappedRoles(scope1.get()))
      } yield {
        s.clientMappings.isEmpty shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  "delete" should "remove the given client-scope objects from the server" in {
    val IO =
      for {
        _ <- EitherT(clientScopeService.delete(scope1.get()))
        _ <- EitherT(clientScopeService.delete(scope2.get()))
        s <- EitherT(clientScopeService.fetch())
      } yield {
        s.map(_.id) should not contain allOf (scope1.get(), scope2.get())
      }

    IO.value.shouldReturnSuccess
  }

  "Delete Ancillary Objects" should "delete all objects needed to test all the ScopeMappings service calls" in {
    val IO =
      for {
        _ <- EitherT(realmRoleService.delete(rRole1Name))
        _ <- EitherT(realmRoleService.delete(rRole2Name))
        _ <- EitherT(clientRoleService.delete(clientUuid.get(), cRole1Name))
        _ <- EitherT(clientRoleService.delete(clientUuid.get(), cRole2Name))
        r <- EitherT(clientService.delete(clientUuid.get()))
      } yield r

    IO.value.shouldReturnSuccess
  }
}
