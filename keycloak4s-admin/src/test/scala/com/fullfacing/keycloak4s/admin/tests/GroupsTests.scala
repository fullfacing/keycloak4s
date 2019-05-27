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
  private val subGroup1: AtomicReference[UUID] = new AtomicReference[UUID]()
  private val storedRoleId: AtomicReference[UUID] = new AtomicReference[UUID]()
  private val storedClientId: AtomicReference[UUID] = new AtomicReference[UUID]()
  /////////////////////////////////////////////////////////////////////

  "Create Ancillary Objects" should "create all objects needed to test all the groups service calls" in {
    val task =
      for {
        _ <- groupService.create(Group.Create("Demo Group 1"))
        _ <- groupService.create(Group.Create("Demo Group 2"))
        _ <- groupService.create(Group.Create("Demo Group 3"))
        _ <- groupService.create(Group.Create("Demo Sub-Group 1"))
        r <- userService.create(User.Create("Demo User 1", enabled = true))
      } yield r

    task.shouldReturnSuccess
  }

  "Fetch Ancillary Object's UUIDs" should "retrieve the created objects and store their IDs" in {
    val task: EitherT[IO, KeycloakError, Unit] =
      for {
        g <- EitherT(groupService.fetch())
        u <- EitherT(userService.fetch())
      } yield {
        group1.set(g.find(_.name == "Demo Group 1").get.id)
        group2.set(g.find(_.name == "Demo Group 2").get.id)
        group3.set(g.find(_.name == "Demo Group 3").get.id)
        subGroup1.set(g.find(_.name == "Demo Sub-Group 1").get.id)
        user1.set(u.find(_.username == "Demo User 1").get.id)
      }

    task.value.shouldReturnSuccess
  }

  "fetch" should "successfully retrieve all groups" in {
    groupService.fetch().map { response =>
      response.map(groups => storedGroups.set(groups))
      response shouldBe a [Right[_, _]]
    }.unsafeToFuture()
  }

  "fetchByName" should "successfully return a sequence of Group model" in {
    groupService.fetch(search = Some("Demo Group 1")).shouldReturnSuccess
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
        ug    <- EitherT(groupService.update(group1.get, Group(id = group.id, name = "Demo Group 2", path = group.path)))
    } yield {
        ug.asInstanceOf[Group].name should contain("Demo Group 2")
      }).value
    task.shouldReturnSuccess
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

  "createSubGroup" should "successfully return a sequence of a Group Model with the new sub-group" in {
    groupService.createSubGroup(group2.get, Group.Create("Demo Sub-Group 2")).shouldReturnSuccess
  }

  "fetchRoles" should "successfully retrieve all Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    groupService.fetchRoles(user.id).map(_.map { roles =>
      roles.realmMappings shouldNot be (empty)
      roles.clientMappings.get("account") shouldNot be (None)
    }).shouldReturnSuccess
  }

  "fetchRealmRoles" should "successfully retrieve all Realm Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    groupService.fetchRealmRoles(user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "addRealmRoles" should "successfully map a Realm Role to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    for {
      _   <- EitherT(realmRoleService.create(Role.Create(name = "test_role", clientRole = false, composite = false)))
      id  <- EitherT(realmRoleService.fetchByName("test_role")).map(_.id)
      _   <- EitherT(groupService.addRealmRoles(user.id, List(Role(id = id, name = "test_role", clientRole = false, composite = false))))
    } yield storedRoleId.set(id)
  }.value.shouldReturnSuccess

  "removeRealmRoles" should "successfully unmap a Realm Role from a User" in {
    val user = storedUsers.get().find(_.username == "admin").get
    val role = Role(name = "test_role", id = storedRoleId.get, clientRole = false, composite = false)

    for {
      _ <- EitherT(groupService.removeRealmRoles(user.id, List(role)))
      _ <- EitherT(realmRoleService.remove("test_role"))
    } yield ()
  }.value.shouldReturnSuccess

  "fetchAvailableRealmRoles" should "successfully retrieve all Realm Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get

    groupService.fetchAvailableRealmRoles(user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "fetchEffectiveRealmRoles" should "successfully retrieve all Realm Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    groupService.fetchEffectiveRealmRoles(user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "fetchClientsRoles" should "successfully retrieve all Client Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    for {
      id    <- EitherT(clientService.fetch(clientId = Some("account"))).map(_.head.id)
      roles <- EitherT(groupService.fetchClientRoles(id, user.id))
    } yield {
      roles shouldNot be (empty)
      storedClientId.set(id)
    }
  }.value.shouldReturnSuccess

  "addClientRoles" should "successfully map a Client Role to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    for {
      _   <- EitherT(clientRoleService.create(storedClientId.get(), Role.Create(name = "test_role", clientRole = false, composite = false)))
      id  <- EitherT(clientRoleService.fetchByName(storedClientId.get(), "test_role")).map(_.id)
      _   <- EitherT(groupService.addClientRoles(storedClientId.get(), user.id, List(Role(id = id, name = "test_role", clientRole = false, composite = false))))
    } yield storedRoleId.set(id)
  }.value.shouldReturnSuccess

  "fetchAvailableClientRoles" should "successfully retrieve all Client Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "test_user1").get

    groupService.fetchAvailableClientRoles(storedClientId.get(), user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "removeClientRoles" should "successfully unmap a Client Role from a User" in {
    val user = storedUsers.get().find(_.username == "admin").get
    val role = Role(name = "test_role", id = storedRoleId.get, clientRole = false, composite = false)

    for {
      _ <- EitherT(groupService.removeClientRoles(storedClientId.get(), user.id, List(role)))
      _ <- EitherT(realmRoleService.remove("test_role"))
    } yield ()
  }.value.shouldReturnSuccess

  "fetchEffectiveClientRoles" should "successfully retrieve all Client Roles mapped to a User" in {
    val user = storedUsers.get().find(_.username == "admin").get

    groupService.fetchEffectiveClientRoles(storedClientId.get(), user.id).map(_.map { roles =>
      roles shouldNot be (empty)
    }).shouldReturnSuccess
  }

  "ManagementPermissions" should "fire fetch and update requests for ManagementPermission model" in {
    val task =
      (for {
        p  <- EitherT(groupService.fetchManagementPermissions(group3.get))
        up <- EitherT(groupService.updateManagementPermissions(group3.get,
                ManagementPermission(Some(false), p.resource, p.scopePermissions)))
      } yield {
        up.enabled should equal(false)
      }).value
      task.shouldReturnSuccess
  }

  "Count a specific number of groups" should "successfully return an expected number of groups" in {
    groupService.count().map( count =>
      count shouldBe 4
    ).unsafeToFuture()
  }

  "Delete Ancillary Objects" should "remove all the ancillary objects created for testing Groups" in {
    val task =
      for {
        _ <- groupService.delete(group1.get())
        _ <- groupService.delete(group2.get())
        _ <- groupService.delete(group3.get())
        _ <- groupService.delete(subGroup1.get())
        r <- userService.delete(user1.get())
      } yield r

    task.shouldReturnSuccess
  }

}
