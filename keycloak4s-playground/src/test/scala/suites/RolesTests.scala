package suites

import cats.data.EitherT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.fullfacing.keycloak4s.core.models._
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class RolesTests extends IntegrationSpec {

  /* Testing functions and data. **/
  private def mapToRoleMapping(id: UUID, role: Role.Create): Role.Mapping =
    Role.Mapping(
      id         = id,
      name       = role.name
    )

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

  private val rRole3Name = "realmRole3"
  val rRole3Create: Role.Create = Role.Create(
    clientRole = false,
    composite  = false,
    name       = rRole3Name
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

  private val cRole3Name = "clientRole3"
  val cRole3Create: Role.Create = Role.Create(
    clientRole = true,
    composite  = false,
    name       = cRole3Name
  )

  private val group1Create = Group.Create("group1")
  private val group2Create = Group.Create("group2")

  private val user1Create = User.Create("user1", enabled = true)
  private val user2Create = User.Create("user2", enabled = true)

  /* References for storing tests results to be used in subsequent tests. **/
  private val rRole1     = new AtomicReference[UUID]()
  private val rRole2     = new AtomicReference[UUID]()
  private val rRole3     = new AtomicReference[UUID]()
  private val cRole1     = new AtomicReference[UUID]()
  private val cRole2     = new AtomicReference[UUID]()
  private val cRole3     = new AtomicReference[UUID]()
  private val clientUuid = new AtomicReference[UUID]()
  private val user1      = new AtomicReference[UUID]()
  private val user2      = new AtomicReference[UUID]()
  private val group1     = new AtomicReference[UUID]()
  private val group2     = new AtomicReference[UUID]()

  "Create Ancillary Objects" should "create all objects needed to test all the roles service calls" in {
    val IO =
      for {
        _ <- clientService.create(Client.Create("RoleTestClient"))
        _ <- groupService.create(group1Create)
        _ <- groupService.create(group2Create)
        _ <- userService.create(user1Create)
        r <- userService.create(user2Create)
      } yield r

    IO.shouldReturnSuccess
  }

  "Fetch Ancillary Object's UUIDs" should "retrieve the created objects and store their IDs" in {
    val IO: EitherT[IO, KeycloakError, Unit] =
      for {
        c <- EitherT(clientService.fetch(clientId = Some("RoleTestClient")))
        g <- EitherT(groupService.fetch())
        u <- EitherT(userService.fetch())
        c1  <- EitherT.fromOption[IO](c.headOption, Errors.CLIENT_NOT_FOUND)
        g1  <- EitherT.fromOption[IO](g.find(_.name == group1Create.name), Errors.GROUP_NOT_FOUND)
        g2  <- EitherT.fromOption[IO](g.find(_.name == group2Create.name), Errors.GROUP_NOT_FOUND)
        u1  <- EitherT.fromOption[IO](u.find(_.username == user1Create.username), Errors.USER_NOT_FOUND)
        u2  <- EitherT.fromOption[IO](u.find(_.username == user2Create.username), Errors.USER_NOT_FOUND)
      } yield {
        clientUuid.set(c1.id)
        group1.set(g1.id)
        group2.set(g2.id)
        user1.set(u1.id)
        user2.set(u2.id)
      }

    IO.value.shouldReturnSuccess
  }

  "Create" should "successfully create realm level roles" in {
    val IO =
      for {
        _ <- realmRoleService.create(rRole1Create)
        r <- realmRoleService.create(rRole2Create)
      } yield r

    IO.shouldReturnSuccess
  }

  it should "successfully create a client and client level roles for that client" in {
    val IO =
      for {
        _ <- clientRoleService.create(clientUuid.get(), cRole1Create)
        r <- clientRoleService.create(clientUuid.get(), cRole2Create)
      } yield r

    IO.shouldReturnSuccess
  }

  "createAndRetrieve" should "create a realm level role and subsequently return it" in {
    realmRoleService.createAndRetrieve(rRole3Create).map(_.map { role =>
      rRole3.set(role.id)
    })
  }.shouldReturnSuccess

  it should "correctly handle error when attempting to create a duplicate Realm Role" in {
    realmRoleService.createAndRetrieve(rRole3Create).map(_.map { role =>
      rRole3.set(role.id)
    })
  }.map(_ shouldBe a [scala.util.Left[_, _]]).unsafeToFuture

  it should "create a client level role and subsequently return it" in {
    clientRoleService.createAndRetrieve(clientUuid.get(), cRole3Create).map(_.map { role =>
      cRole3.set(role.id)
    })
  }.shouldReturnSuccess

  it should "correctly handle error when attempting to create a duplicate Client Role" in {
    clientRoleService.createAndRetrieve(clientUuid.get(), cRole3Create).map(_.map { role =>
      cRole3.set(role.id)
    })
  }.map(_ shouldBe a [scala.util.Left[_, _]]).unsafeToFuture

  "Fetch" should "successfully fetch the previously created realm roles" in {
    val IO =
      for {
        _  <- EitherT(realmRoleService.fetch())
        r1 <- EitherT(realmRoleService.fetchByName(rRole1Name))
        r2 <- EitherT(realmRoleService.fetchByName(rRole2Name))
      } yield {
        rRole1.set(r1.id)
        rRole2.set(r2.id)
      }

    IO.value.shouldReturnSuccess
  }

  it should "successfully fetch the previously created client roles" in {
    val IO =
      for {
        _  <- EitherT(clientRoleService.fetch(clientUuid.get()))
        c1 <- EitherT(clientRoleService.fetchByName(clientUuid.get(), cRole1Name))
        c2 <- EitherT(clientRoleService.fetchByName(clientUuid.get(), cRole2Name))
      } yield {
        cRole1.set(c1.id)
        cRole2.set(c2.id)

        c1.name shouldBe cRole1Name
        c2.name shouldBe cRole2Name
      }

    IO.value.shouldReturnSuccess
  }

  "Update" should "successfully update a realm role" in {
    val updatedDescription = "This is the description"
    val updatedName        = "TestUpdate"
    val IO =
      for {
        _  <- EitherT(realmRoleService.update(rRole1Name, Role.Update(name = rRole1Name, description = Some(updatedDescription))))
        _  <- EitherT(realmRoleService.update(rRole2Name, Role.Update(name = updatedName)))
        r1 <- EitherT(realmRoleService.fetchByName(rRole1Name))
        r2 <- EitherT(realmRoleService.fetchByName(updatedName))
        _  <- EitherT(realmRoleService.update(updatedName, Role.Update(name = rRole2Name))) //reset role
      } yield {
        r1.description shouldBe Some(updatedDescription)
        r2.name        shouldBe updatedName
      }

    IO.value.shouldReturnSuccess
  }

  it should "successfully update a client role" in {
    val updatedDescription = "This is the description"
    val updatedName        = "TestUpdate"
    val IO =
      for {
        _  <- EitherT(clientRoleService.update(clientUuid.get(), cRole1Name, Role.Update(name = cRole1Name, description = Some(updatedDescription))))
        _  <- EitherT(clientRoleService.update(clientUuid.get(), cRole2Name, Role.Update(name = updatedName)))
        c1 <- EitherT(clientRoleService.fetchByName(clientUuid.get(), cRole1Name))
        c2 <- EitherT(clientRoleService.fetchByName(clientUuid.get(), updatedName))
        _  <- EitherT(clientRoleService.update(clientUuid.get(), updatedName, Role.Update(name = cRole2Name))) //reset role
      } yield {
        c1.description shouldBe Some(updatedDescription)
        c2.name        shouldBe updatedName
      }

    IO.value.shouldReturnSuccess
  }

  "Composite Realm Role" should "successfully add sub roles to a realm role" in {
    realmRoleService.addCompositeRoles(rRole1Name, List(rRole2.get(), cRole1.get))
      .shouldReturnSuccess
  }

  it should "successfully retrieve sub roles added to the realm role" in {
    val IO =
      for {
        a <- EitherT(realmRoleService.fetchCompositeRoles(rRole1Name))
        r <- EitherT(realmRoleService.fetchRealmCompositeRoles(rRole1Name))
        c <- EitherT(realmRoleService.fetchClientCompositeRoles(rRole1Name, clientUuid.get()))
      } yield {
        a.nonEmpty                     shouldBe true
        r.exists(_.id == rRole2.get()) shouldBe true
        c.exists(_.id == cRole1.get()) shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  it should "successfully remove all sub roles from the realm role" in {
    val IO =
      for {
        _  <- EitherT(realmRoleService.removeCompositeRoles(rRole1Name, List(rRole2.get(), cRole1.get())))
        r  <- EitherT(realmRoleService.fetchCompositeRoles(rRole1Name))
      } yield r.isEmpty shouldBe true

    IO.value.shouldReturnSuccess
  }

  "Composite Client Role" should "successfully add sub roles to a client role" in {
    clientRoleService.addCompositeRoles(clientUuid.get(), cRole1Name, List(rRole1.get(), cRole2.get))
      .shouldReturnSuccess
  }

  it should "successfully retrieve sub roles added to the client role" in {
    val IO =
      for {
        a <- EitherT(clientRoleService.fetchCompositeRoles(clientUuid.get(), cRole1Name))
        r <- EitherT(clientRoleService.fetchRealmCompositeRoles(clientUuid.get(), cRole1Name))
        c <- EitherT(clientRoleService.fetchClientCompositeRoles(clientUuid.get(), cRole1Name, clientUuid.get()))
      } yield {
        a.nonEmpty                     shouldBe true
        r.exists(_.id == rRole1.get()) shouldBe true
        c.exists(_.id == cRole2.get()) shouldBe true
      }

    IO.value.shouldReturnSuccess
  }

  it should "successfully remove all sub roles from the client role" in {
    val IO =
      for {
        _  <- EitherT(clientRoleService.removeCompositeRoles(clientUuid.get(), cRole1Name, List(rRole1.get(), cRole2.get())))
        r  <- EitherT(clientRoleService.fetchCompositeRoles(clientUuid.get(), cRole1Name))
      } yield r.isEmpty shouldBe true

    IO.value.shouldReturnSuccess
  }

  "Management Permissions" should "do something or another to a realm role" in {
    val IO =
      for {
        _ <- EitherT(realmRoleService.fetchManagementPermissions(rRole1Name))
        b <- EitherT(realmRoleService.enableManagementPermissions(rRole1Name))
        c <- EitherT(realmRoleService.disableManagementPermissions(rRole1Name))
      } yield {
        b.enabled shouldBe true
        c.enabled shouldBe false
      }

    IO.value.shouldReturnSuccess
  }

  it should "do something or another to a client role" in {
    val IO =
      for {
        _ <- EitherT(clientRoleService.fetchManagementPermissions(clientUuid.get(), cRole1Name))
        b <- EitherT(clientRoleService.enableManagementPermissions(clientUuid.get(), cRole1Name))
        c <- EitherT(clientRoleService.disableManagementPermissions(clientUuid.get(), cRole1Name))
      } yield {
        b.enabled shouldBe true
        c.enabled shouldBe false
      }

    IO.value.shouldReturnSuccess
  }


  "Fetch Users" should "successfully retrieve all users who have a realm role" in {
    val r1 = mapToRoleMapping(rRole1.get(), rRole1Create)
    val r2 = mapToRoleMapping(rRole2.get(), rRole2Create)
    val IO =
      for {
        _ <- EitherT(userService.addRealmRoles(user1.get(), List(r1, r2)))
        _ <- EitherT(userService.addRealmRoles(user2.get(), List(r1)))
        a <- EitherT(realmRoleService.fetchUsers(rRole1Name, None, None))
        b <- EitherT(realmRoleService.fetchUsers(rRole2Name, None, None))
        _ <- EitherT(userService.removeRealmRoles(user1.get(), List(r1, r2)))
        _ <- EitherT(userService.removeRealmRoles(user2.get(), List(r1)))
      } yield {
        a.size shouldBe 2
        b.size shouldBe 1
      }

    IO.value.shouldReturnSuccess
  }

  it should "successfully retrieve all users who have a client role" in {
    val r1 = mapToRoleMapping(cRole1.get(), cRole1Create)
    val r2 = mapToRoleMapping(cRole2.get(), cRole2Create)
    val IO =
      for {
        _ <- EitherT(userService.addClientRoles(clientUuid.get(), user1.get(), List(r1, r2)))
        _ <- EitherT(userService.addClientRoles(clientUuid.get(), user2.get(), List(r1)))
        a <- EitherT(clientRoleService.fetchUsers(clientUuid.get(), cRole1Name, None, None))
        b <- EitherT(clientRoleService.fetchUsers(clientUuid.get(), cRole2Name, None, None))
        _ <- EitherT(userService.removeClientRoles(clientUuid.get(), user1.get(), List(r1, r2)))
        _ <- EitherT(userService.removeClientRoles(clientUuid.get(), user2.get(), List(r1)))
      } yield {
        a.size shouldBe 2
        b.size shouldBe 1
      }

    IO.value.shouldReturnSuccess
  }

  "Fetch Groups" should "successfully retrieve all groups that a given realm role" in {
    val r1 = mapToRoleMapping(rRole1.get(), rRole1Create)
    val r2 = mapToRoleMapping(rRole2.get(), rRole2Create)
    val IO =
      for {
        _ <- EitherT(groupService.addRealmRoles(group1.get(), List(r1, r2)))
        _ <- EitherT(groupService.addRealmRoles(group2.get(), List(r1)))
        a <- EitherT(realmRoleService.fetchGroups(rRole1Name, None, None, Some(true)))
        b <- EitherT(realmRoleService.fetchGroups(rRole2Name, None, None, Some(false)))
        _ <- EitherT(groupService.removeRealmRoles(group1.get(), List(r1, r2)))
        _ <- EitherT(groupService.removeRealmRoles(group2.get(), List(r1)))
      } yield {
        a.size shouldBe 2
        b.size shouldBe 1
      }

    IO.value.shouldReturnSuccess
  }

  it should "successfully retrieve all groups that have a client role" in {
    val r1 = mapToRoleMapping(cRole1.get(), cRole1Create)
    val r2 = mapToRoleMapping(cRole2.get(), cRole2Create)
    val IO =
      for {
        _ <- EitherT(groupService.addClientRoles(clientUuid.get(), group1.get(), List(r1, r2)))
        _ <- EitherT(groupService.addClientRoles(clientUuid.get(), group2.get(), List(r1)))
        a <- EitherT(clientRoleService.fetchGroups(clientUuid.get(), cRole1Name, None, None, Some(true)))
        b <- EitherT(clientRoleService.fetchGroups(clientUuid.get(), cRole2Name, None, None, Some(false)))
        _ <- EitherT(groupService.removeClientRoles(clientUuid.get(), group1.get(), List(r1, r2)))
        _ <- EitherT(groupService.removeClientRoles(clientUuid.get(), group2.get(), List(r1)))
      } yield {
        a.size shouldBe 2
        b.size shouldBe 1
      }

    IO.value.shouldReturnSuccess
  }

  "Delete" should "successfully remove roles from the realm" in {
    val IO =
      for {
        _ <- realmRoleService.delete(rRole1Name)
        _ <- realmRoleService.delete(rRole2Name)
        _ <- realmRoleService.delete(rRole3Name)
      } yield Right(())

    IO.shouldReturnSuccess
  }

  it should "successfully remove roles from the client" in {
    val IO =
      for {
        _ <- clientRoleService.delete(clientUuid.get(), cRole1Name)
        _ <- clientRoleService.delete(clientUuid.get(), cRole2Name)
        r <- clientRoleService.delete(clientUuid.get(), cRole3Name)
      } yield r

    IO.shouldReturnSuccess
  }

  "Delete Ancillary Objects" should "remove all the ancillary objects created for testing Roles" in {
    val IO =
      for {
        _ <- clientService.delete(clientUuid.get())
        _ <- userService.delete(user1.get())
        _ <- userService.delete(user2.get())
        _ <- groupService.delete(group1.get())
        r <- groupService.delete(group2.get())
      } yield r

    IO.shouldReturnSuccess
  }
}
