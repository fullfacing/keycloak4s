package com.fullfacing.keycloak4s.admin.tests

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.data.EitherT
import cats.effect.IO
import com.fullfacing.keycloak4s.admin.IntegrationSpec
import com.fullfacing.keycloak4s.core.models._
import org.scalatest.DoNotDiscover

@DoNotDiscover
class GroupsTests extends IntegrationSpec {

  val storedGroups: AtomicReference[Seq[Group]] = new AtomicReference[Seq[Group]]()
  val storedUsers: AtomicReference[List[User]] = new AtomicReference[List[User]]()

  //IDs of objects created in tests////////////////////////////////////
  private val user1: AtomicReference[UUID] = new AtomicReference[UUID]()
  private val group1: AtomicReference[UUID] = new AtomicReference[UUID]()
  private val group2: AtomicReference[UUID] = new AtomicReference[UUID]()
  private val group3: AtomicReference[UUID] = new AtomicReference[UUID]()
  private val storedRoleId: AtomicReference[UUID] = new AtomicReference[UUID]()
  private val storedClientId: AtomicReference[UUID] = new AtomicReference[UUID]()
  /////////////////////////////////////////////////////////////////////

  "Create Ancillary Objects" should "create all objects needed to test all the groups service calls" in {
    val task =
      for {
        _ <- groupService.create(Group.Create("Group 1"))
        _ <- groupService.create(Group.Create("Group 2"))
        _ <- groupService.create(Group.Create("Group 3"))
        r <- userService.create(User.Create(username = "user1", enabled = true))
      } yield r

    task.shouldReturnSuccess
  }

  "Fetch Ancillary Object's UUIDs" should "retrieve the created objects and store their IDs" in {
    val task: EitherT[IO, KeycloakError, Unit] =
      for {
        g <- EitherT(groupService.fetch())
        u <- EitherT(userService.fetch())
      } yield {
        group1.set(g.find(_.name == "Group 1").get.id)
        group2.set(g.find(_.name == "Group 2").get.id)
        group3.set(g.find(_.name == "Group 3").get.id)
        user1.set(u.find(_.username == "user1").get.id)
      }

    task.value.shouldReturnSuccess
  }

  "fetchGroups" should "successfully retrieve all groups" in {
    groupService.fetch().map { response =>
      response.map(groups => storedGroups.set(groups))
      response shouldBe a [Right[_, _]]
    }.unsafeToFuture()
  }

  "fetchUsers" should "successfully retrieve all users" in {
    userService.fetch().map { response =>
      response.map(users => storedUsers.set(users))
      response shouldBe a [Right[_, _]]
    }.unsafeToFuture()
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
    val task =
      (for {
        group <- EitherT(groupService.fetchById(group1.get))
        _     <- EitherT(groupService.update(group1.get, Group("Group 4", group.path, id = group1.get)))
        ug    <- EitherT(groupService.fetchById(group1.get))
    } yield {
        ug.name should equal("Group 4")
      }).value
    task.shouldReturnSuccess
  }

  "createSubGroup" should "successfully return a sequence of a Group Model with the new sub-group" in {
    groupService.createSubGroup(group2.get, Group.Create("Sub-Group 2")).shouldReturnSuccess
  }

  "fetchRoles" should "successfully retrieve all Roles mapped to a Group" in {
    groupService.fetchRoles(group2.get).map(_.map { roles =>
      roles.realmMappings should be (empty)
    }).shouldReturnSuccess
  }

  "fetchRealmRoles" should "successfully retrieve all Realm Roles mapped to a Group" in {
    groupService.fetchRealmRoles(group2.get).map(_.map { roles =>
      roles should be (empty)
    }).shouldReturnSuccess
  }

  "addRealmRoles" should "successfully map a Realm Role to a Group" in {
    for {
      _   <- EitherT(realmRoleService.create(Role.Create(name = "test_role1", clientRole = false, composite = false)))
      id  <- EitherT(realmRoleService.fetchByName("test_role1")).map(_.id)
      _   <- EitherT(groupService.addRealmRoles(group2.get, List(Role(id = id, name = "test_role1", clientRole = false, composite = false))))
    } yield storedRoleId.set(id)
  }.value.shouldReturnSuccess

  "fetchAvailableRealmRoles" should "successfully retrieve all Realm Roles mapped to a Group" in {
    groupService.fetchAvailableRealmRoles(group2.get).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "fetchEffectiveRealmRoles" should "successfully retrieve all Realm Roles mapped to a Group" in {
    groupService.fetchEffectiveRealmRoles(group2.get).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "removeRealmRoles" should "successfully unmap a Realm Role from a Group" in {
    val role = Role(name = "test_role1", id = storedRoleId.get, clientRole = false, composite = false)

    for {
      _ <- EitherT(groupService.removeRealmRoles(group2.get, List(role)))
      _ <- EitherT(realmRoleService.remove("test_role1"))
    } yield ()
  }.value.shouldReturnSuccess

  "fetchClientsRoles" should "successfully retrieve all Client Roles mapped to a Group" in {
    for {
      id    <- EitherT(clientService.fetch(clientId = Some("account"))).map(_.head.id)
      roles <- EitherT(groupService.fetchClientRoles(id, group2.get))
    } yield {
      roles should be (empty)
      storedClientId.set(id)
    }
  }.value.shouldReturnSuccess

  "addClientRoles" should "successfully map a Client Role to a Group" in {
    for {
      _   <- EitherT(clientRoleService.create(storedClientId.get(), Role.Create(name = "test_role1", clientRole = false, composite = false)))
      id  <- EitherT(clientRoleService.fetchByName(storedClientId.get(), "test_role1")).map(_.id)
      _   <- EitherT(groupService.addClientRoles(storedClientId.get(), group2.get, List(Role(id = id, name = "test_role1", clientRole = false, composite = false))))
    } yield storedRoleId.set(id)
  }.value.shouldReturnSuccess

  "fetchAvailableClientRoles" should "successfully retrieve all Client Roles mapped to a Group" in {
    groupService.fetchAvailableClientRoles(storedClientId.get(), group2.get).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "fetchEffectiveClientRoles" should "successfully retrieve all Client Roles mapped to a Group" in {
    groupService.fetchEffectiveClientRoles(storedClientId.get(), group2.get).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "removeClientRoles" should "successfully unmap a Client Role from a Group" in {
    val role = Role(name = "test_role1", id = storedRoleId.get, clientRole = false, composite = false)

    for {
      _ <- EitherT(groupService.removeClientRoles(storedClientId.get(), group2.get, List(role)))
      _ <- EitherT(clientRoleService.remove(storedClientId.get(), role.name))
    } yield ()
  }.value.shouldReturnSuccess

  "ManagementPermissions" should "fire fetch and update requests for ManagementPermission model" in {
    val task =
      (for {
        p  <- EitherT(groupService.fetchManagementPermissions(group3.get))
        up <- EitherT(groupService.updateManagementPermissions(group3.get,
                ManagementPermission(enabled = false, p.resource, p.scopePermissions)))
      } yield {
        up.enabled should equal(false)
      }).value
      task.shouldReturnSuccess
  }

  "Count a specific number of groups" should "successfully return an expected number of groups" in {
    groupService.count().map(_.map( count =>
      count shouldBe Count(5)
    )).shouldReturnSuccess
  }

  "Group level User calls" should "successfully fire different requests for users belonging to a specific group" in {
    val task =
      (for {
        _     <- EitherT(groupService.addUserToGroup(user1.get, group2.get))
        _     <- EitherT(groupService.fetchUsers(group2.get))
        _     <- EitherT(groupService.removeUserFromGroup(user1.get, group2.get))
      } yield ()).value
    task.shouldReturnSuccess
  }

  "Delete Ancillary Objects" should "remove all the ancillary objects created for testing Groups" in {
    val task =
      for {
        _ <- groupService.delete(group1.get)
        _ <- groupService.delete(group2.get)
        _ <- groupService.delete(group3.get)
        r <- userService.delete(user1.get)
      } yield r

    task.shouldReturnSuccess
  }

}
