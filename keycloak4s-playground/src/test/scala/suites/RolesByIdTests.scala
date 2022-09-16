package suites

import cats.data.EitherT
import cats.effect.IO
import com.fullfacing.keycloak4s.core.models.Role
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class RolesByIdTests extends IntegrationSpec {

  /* Testing functions and data. **/
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

  /* References for storing tests results to be used in subsequent tests. **/
  private val cId    = new AtomicReference[UUID]()
  private val rRole1 = new AtomicReference[UUID]()
  private val rRole2 = new AtomicReference[UUID]()
  private val cRole1 = new AtomicReference[UUID]()
  private val cRole2 = new AtomicReference[UUID]()


  "Test setup" should "create the roles necessary to perform these tests" in {
    val IO =
      for {
        c   <- EitherT(clientService.fetch(clientId = Some("admin-cli")))
        c1  <- EitherT.fromOption[IO](c.headOption, Errors.NO_CLIENTS_FOUND)
        _   =  cId.set(c1.id)
        _   <- EitherT(realmRoleService.create(rRole1Create))
        _   <- EitherT(realmRoleService.create(rRole2Create))
        _   <- EitherT(clientRoleService.create(cId.get(), cRole1Create))
        _   <- EitherT(clientRoleService.create(cId.get(), cRole2Create))
      } yield ()

    IO.value.shouldReturnSuccess
  }

  it should "fetch the created roles and store their IDs" in {
    val IO =
      for {
        r <- EitherT(realmRoleService.fetch())
        c <- EitherT(clientRoleService.fetch(cId.get()))
      } yield {
        rRole1.set(r.find(_.name == rRole1Name).get.id)
        rRole2.set(r.find(_.name == rRole2Name).get.id)
        cRole1.set(c.find(_.name == cRole1Name).get.id)
        cRole2.set(c.find(_.name == cRole2Name).get.id)
      }

    IO.value.shouldReturnSuccess
  }

  "fetch" should "successfully retrieve the requested roles" in {
    val IO =
      for {
        r1 <- EitherT(rolesByIdService.fetch(rRole1.get()))
        r2 <- EitherT(rolesByIdService.fetch(rRole2.get()))
        c1 <- EitherT(rolesByIdService.fetch(cRole1.get()))
        c2 <- EitherT(rolesByIdService.fetch(cRole2.get()))
      } yield {
        r1.name shouldBe rRole1Name
        r2.name shouldBe rRole2Name
        c1.name shouldBe cRole1Name
        c2.name shouldBe cRole2Name
      }

    IO.value.shouldReturnSuccess
  }

  "update" should "successfully update specified values of the given role" in {
    val updatedDescription = Some("New description")
    val IO =
      for {
        _ <- EitherT(rolesByIdService.update(rRole1.get(), Role.Update(name = rRole1Name, description = updatedDescription)))
        _ <- EitherT(rolesByIdService.update(cRole1.get(), Role.Update(name = cRole1Name, description = updatedDescription)))
        r <- EitherT(rolesByIdService.fetch(rRole1.get()))
        c <- EitherT(rolesByIdService.fetch(cRole1.get()))
      } yield {
        r.description shouldBe updatedDescription
        c.description shouldBe updatedDescription
      }

    IO.value.shouldReturnSuccess
  }

  "addCompositeRoles" should "successfully add sub roles to a given role" in {
    val IO=
      for {
        _ <- rolesByIdService.addCompositeRoles(rRole1.get(), List(rRole2.get(), cRole2.get()))
        r <- rolesByIdService.addCompositeRoles(cRole1.get(), List(rRole2.get(), cRole2.get()))
      } yield r

    IO.shouldReturnSuccess
  }

  "fetchCompositeRoles" should "successfully retrieve all sub roles of the given role" in {
    val IO =
      for {
        r <- EitherT(rolesByIdService.fetchCompositeRoles(rRole1.get()))
        c <- EitherT(rolesByIdService.fetchCompositeRoles(cRole1.get()))
      } yield {
        r.size shouldBe 2
        c.size shouldBe 2
      }

    IO.value.shouldReturnSuccess
  }

  "fetchClientLevelCompositeRoles" should "successfully retrieve all client level sub roles of the given role" in {
    val IO =
      for {
        r <- EitherT(rolesByIdService.fetchClientLevelCompositeRoles(rRole1.get(), cId.get()))
        c <- EitherT(rolesByIdService.fetchClientLevelCompositeRoles(cRole1.get(), cId.get()))
      } yield {
        r.size shouldBe 1
        c.size shouldBe 1
      }

    IO.value.shouldReturnSuccess
  }

  "fetchRealmLevelCompositeRoles" should "successfully retrieve all realm level sub roles of the given role" in {
    val IO =
      for {
        r <- EitherT(rolesByIdService.fetchRealmLevelCompositeRoles(rRole1.get()))
        c <- EitherT(rolesByIdService.fetchRealmLevelCompositeRoles(cRole1.get()))
      } yield {
        r.size shouldBe 1
        c.size shouldBe 1
      }

    IO.value.shouldReturnSuccess
  }

  "removeCompositeRoles" should "successfully remove all specified sub roles from the given role" in {
    val IO =
      for {
        _ <- EitherT(rolesByIdService.removeCompositeRoles(rRole1.get(), List(rRole2.get(), cRole2.get())))
        _ <- EitherT(rolesByIdService.removeCompositeRoles(cRole1.get(), List(rRole2.get(), cRole2.get())))
        r <- EitherT(rolesByIdService.fetchCompositeRoles(rRole1.get()))
        c <- EitherT(rolesByIdService.fetchCompositeRoles(cRole1.get()))
      } yield {
        r.size shouldBe 0
        c.size shouldBe 0
      }

    IO.value.shouldReturnSuccess
  }

  "fetchManagementPermissions" should "return the management permissions of the role stating whether it is enabled or not" in {
    val IO =
      for {
        _ <- rolesByIdService.fetchManagementPermissions(rRole1.get())
        r <- rolesByIdService.fetchManagementPermissions(cRole1.get())
      } yield r

    IO.shouldReturnSuccess
  }

  "enableManagementPermissions" should "successfully initialise the role's auth permissions" in {
    val IO =
      for {
        _ <- EitherT(rolesByIdService.enableManagementPermissions(rRole1.get()))
        _ <- EitherT(rolesByIdService.enableManagementPermissions(cRole1.get()))
        r <- EitherT(rolesByIdService.fetchManagementPermissions(rRole1.get()))
        c <- EitherT(rolesByIdService.fetchManagementPermissions(cRole1.get()))
      } yield {
        r.enabled shouldBe true
        c.enabled shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  "disableManagementPermissions" should "successfully disable the role's auth permissions" in {
    val IO =
      for {
        _ <- EitherT(rolesByIdService.disableManagementPermissions(rRole1.get()))
        _ <- EitherT(rolesByIdService.disableManagementPermissions(cRole1.get()))
        r <- EitherT(rolesByIdService.fetchManagementPermissions(rRole1.get()))
        c <- EitherT(rolesByIdService.fetchManagementPermissions(cRole1.get()))
      } yield {
        r.enabled shouldBe false
        c.enabled shouldBe false
      }

    IO.value.shouldReturnSuccess
  }

  "delete" should "successfully delete all specified roles" in {
    val IO =
      for {
        _ <- rolesByIdService.delete(rRole1.get())
        _ <- rolesByIdService.delete(rRole2.get())
        _ <- rolesByIdService.delete(cRole1.get())
        r <- rolesByIdService.delete(cRole2.get())
      } yield r

    IO.shouldReturnSuccess
  }
}
