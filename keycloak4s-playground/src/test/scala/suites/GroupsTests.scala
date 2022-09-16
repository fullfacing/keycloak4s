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
class GroupsTests extends IntegrationSpec {

  /* References for storing tests results to be used in subsequent tests. **/
  val storedGroups: AtomicReference[Seq[Group]] = new AtomicReference[Seq[Group]]()
  val storedUsers: AtomicReference[List[User]]  = new AtomicReference[List[User]]()
  val user1: AtomicReference[UUID]              = new AtomicReference[UUID]()
  val group1: AtomicReference[UUID]             = new AtomicReference[UUID]()
  val group2: AtomicReference[UUID]             = new AtomicReference[UUID]()
  val group3: AtomicReference[UUID]             = new AtomicReference[UUID]()
  val group4: AtomicReference[UUID]             = new AtomicReference[UUID]()
  val storedRoleId: AtomicReference[UUID]       = new AtomicReference[UUID]()
  val storedClientId: AtomicReference[UUID]     = new AtomicReference[UUID]()

  "Create Ancillary Objects" should "create all objects needed to test all the groups service calls" in {
    val IO =
      for {
        _ <- groupService.create(Group.Create("Group 1"))
        _ <- groupService.create(Group.Create("Group 2"))
        _ <- groupService.create(Group.Create("Group 3"))
        r <- userService.create(User.Create(username = "user1", enabled = true))
      } yield r

    IO.shouldReturnSuccess
  }

  "Fetch Ancillary Object's UUIDs" should "retrieve the created objects and store their IDs" in {
    val IO: EitherT[IO, KeycloakError, Unit] =
      for {
        g   <- EitherT(groupService.fetch())
        u   <- EitherT(userService.fetch())
        u1  <- EitherT.fromOption[IO](u.find(_.username == "user1"), Errors.USER_NOT_FOUND)
        g1  <- EitherT.fromOption[IO](g.find(_.name == "Group 1"), Errors.GROUP_NOT_FOUND)
        g2  <- EitherT.fromOption[IO](g.find(_.name == "Group 2"), Errors.GROUP_NOT_FOUND)
        g3  <- EitherT.fromOption[IO](g.find(_.name == "Group 3"), Errors.GROUP_NOT_FOUND)
      } yield {
        group1.set(g1.id)
        group2.set(g2.id)
        group3.set(g3.id)
        user1.set(u1.id)
      }

    IO.value.shouldReturnSuccess
  }

  "createAndRetrieve" should "create a Group and subsequently return it" in {
    groupService.createAndRetrieve(Group.Create("Group 4")).map(_.map { group =>
      group4.set(group.id)
    })
  }.shouldReturnSuccess

  it should "correctly handle error when attempting to create a duplicate Group" in {
    groupService.createAndRetrieve(Group.Create("Group 4")).map(_.map { group =>
      group4.set(group.id)
    })
  }.map(_ shouldBe a [scala.util.Left[_, _]]).unsafeToFuture()


  "fetchGroups" should "successfully retrieve all groups" in {
    groupService.fetch().map(_.map { response =>
      storedGroups.set(response)
    }).shouldReturnSuccess
  }

  "fetchUsers" should "successfully retrieve all users" in {
    userService.fetch().map(_.map { response =>
      storedUsers.set(response)
    }).shouldReturnSuccess
  }

  "fetchByName" should "successfully return a sequence of Group model" in {
    groupService.fetch(search = Some("Group 1")).shouldReturnSuccess
  }

  "fetchById" should "successfully return a sequence of Group model" in {
    groupService.fetchById(group1.get).shouldReturnSuccess
  }

  "count" should "successfully return a sequence of Groups count" in {
    groupService.count(top = true).shouldReturnSuccess
  }

  "Group CRUD" should "successfully fire different requests for a group" in {
    val IO =
      (for {
        group <- EitherT(groupService.fetchById(group1.get()))
        _     <- EitherT(groupService.update(group1.get(), Group.Update(Some("Group 4 NewName"))))
        ug    <- EitherT(groupService.fetchById(group1.get()))
    } yield {
        group.name should not be ug.name
        ug.name should equal("Group 4 NewName")
      }).value
    IO.shouldReturnSuccess
  }

  "createSubGroup" should "successfully return a sequence of a Group Model with the new sub-group" in {
    groupService.createSubGroup(group2.get(), Group.Create("Sub-Group 2")).shouldReturnSuccess
  }

  "fetchRoles" should "successfully retrieve all Roles mapped to a Group" in {
    groupService.fetchRoles(group2.get()).map(_.map { roles =>
      roles.realmMappings should be (empty)
    }).shouldReturnSuccess
  }

  "fetchRealmRoles" should "successfully retrieve all Realm Roles mapped to a Group" in {
    groupService.fetchRealmRoles(group2.get()).map(_.map { roles =>
      roles should be (empty)
    }).shouldReturnSuccess
  }

  "addRealmRoles" should "successfully map a Realm Role to a Group" in {
    for {
      _   <- EitherT(realmRoleService.create(Role.Create(name = "test_role1", clientRole = false, composite = false)))
      id  <- EitherT(realmRoleService.fetchByName("test_role1")).map(_.id)
      _   <- EitherT(groupService.addRealmRoles(group2.get(), List(Role.Mapping(id, "test_role1"))))
    } yield storedRoleId.set(id)
  }.value.shouldReturnSuccess

  "fetchAvailableRealmRoles" should "successfully retrieve all Realm Roles mapped to a Group" in {
    groupService.fetchAvailableRealmRoles(group2.get()).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "fetchEffectiveRealmRoles" should "successfully retrieve all Realm Roles mapped to a Group" in {
    groupService.fetchEffectiveRealmRoles(group2.get()).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "removeRealmRoles" should "successfully unmap a Realm Role from a Group" in {
    val role = Role(name = "test_role1", id = storedRoleId.get(), clientRole = false, composite = false)

    for {
      _ <- EitherT(groupService.removeRealmRoles(group2.get(), List(Role.Mapping(role.id, role.name))))
      _ <- EitherT(realmRoleService.delete("test_role1"))
    } yield ()
  }.value.shouldReturnSuccess

  "fetchClientsRoles" should "successfully retrieve all Client Roles mapped to a Group" in {
    for {
      clients <- EitherT(clientService.fetch(clientId = Some("account")))
      id      <- EitherT.fromOption[IO](clients.headOption.map(_.id), Errors.NO_CLIENTS_FOUND)
      roles   <- EitherT(groupService.fetchClientRoles(id, group2.get()))
    } yield {
      roles should be (empty)
      storedClientId.set(id)
    }
  }.value.shouldReturnSuccess

  "addClientRoles" should "successfully map a Client Role to a Group" in {
    for {
      _   <- EitherT(clientRoleService.create(storedClientId.get(), Role.Create(name = "test_role1", clientRole = false, composite = false)))
      id  <- EitherT(clientRoleService.fetchByName(storedClientId.get(), "test_role1")).map(_.id)
      _   <- EitherT(groupService.addClientRoles(storedClientId.get(), group2.get(), List(Role.Mapping(id, "test_role1"))))
    } yield storedRoleId.set(id)
  }.value.shouldReturnSuccess

  "fetchAvailableClientRoles" should "successfully retrieve all Client Roles mapped to a Group" in {
    groupService.fetchAvailableClientRoles(storedClientId.get(), group2.get()).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "fetchEffectiveClientRoles" should "successfully retrieve all Client Roles mapped to a Group" in {
    groupService.fetchEffectiveClientRoles(storedClientId.get(), group2.get()).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "removeClientRoles" should "successfully unmap a Client Role from a Group" in {
    val role = Role(name = "test_role1", id = storedRoleId.get(), clientRole = false, composite = false)

    for {
      _ <- EitherT(groupService.removeClientRoles(storedClientId.get(), group2.get(), List(Role.Mapping(role.id, role.name))))
      _ <- EitherT(clientRoleService.delete(storedClientId.get(), role.name))
    } yield ()
  }.value.shouldReturnSuccess

  "ManagementPermissions" should "fire fetch and update requests for ManagementPermission model" in {
    val IO =
      (for {
        _  <- EitherT(groupService.fetchManagementPermissions(group3.get()))
        ep <- EitherT(groupService.enableManagementPermissions(group3.get()))
        dp <- EitherT(groupService.disableManagementPermissions(group3.get()))
      } yield {
        ep.enabled should equal(true)
        dp.enabled should equal(false)
      }).value
      IO.shouldReturnSuccess
  }

  "Count a specific number of groups" should "successfully return an expected number of groups" in {
    groupService.count().map(_.map( count =>
      count shouldBe Count(5)
    )).shouldReturnSuccess
  }

  "Group level User calls" should "successfully fire different requests for users belonging to a specific group" in {
    val IO =
      (for {
        _     <- EitherT(groupService.addUserToGroup(user1.get(), group2.get()))
        _     <- EitherT(groupService.fetchUsers(group2.get()))
        _     <- EitherT(groupService.removeUserFromGroup(user1.get(), group2.get()))
      } yield ()).value
    IO.shouldReturnSuccess
  }

  "Delete Ancillary Objects" should "remove all the ancillary objects created for testing Groups" in {
    val IO =
      for {
        _ <- groupService.delete(group1.get())
        _ <- groupService.delete(group2.get())
        _ <- groupService.delete(group3.get())
        _ <- groupService.delete(group4.get())
        r <- userService.delete(user1.get())
      } yield r

    IO.shouldReturnSuccess
  }

}
