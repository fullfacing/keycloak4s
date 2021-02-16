package suites.authz

import com.fullfacing.keycloak4s.authz.monix.bio.models.PermissionTicket
import com.fullfacing.keycloak4s.core.models.{Resource, Scope, User}
import org.scalatest.DoNotDiscover
import suites.authz.IoIntegration._

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class PermissionTests extends IoIntegration {

  val ticket: AtomicReference[PermissionTicket] = new AtomicReference[PermissionTicket]()
  val resourceId: AtomicReference[UUID] = new AtomicReference[UUID]()

  "request" should "return successfully" in {
    (for {
      r1 <- resource
      r2 <- r1.create(Resource.Create("PermissionResource", ownerManagedAccess = Some(true), scopes = Some(Scope.Create("PermissionScope") :: Nil)))
      _   = resourceId.set(r2._id)
      p1 <- permission
      p2 <- p1.request(PermissionTicket.Request(r2._id.toString))
    } yield p2)
      .shouldReturnSuccess
  }

  "create" should "successfully create a new permission ticket" in {
    val create = PermissionTicket.Create(
      requesterName = Some("PermissionTestsUser"),
      resource      = resourceId.get(),
      scopeName     = Some("PermissionScope"),
      granted       = false
    )

    (for {
      _  <- AuthzRealm.userService.create(User.Create("PermissionTestsUser", enabled = true))
      p1 <- permission
      p2 <- p1.create(create)
    } yield ticket.set(p2))
      .shouldReturnSuccess
  }

  "update" should "successfully update the given permission ticket" in {
    permission
      .flatMap(_.update(ticket.get().copy(granted = true)))
      .shouldReturnSuccess
  }

  "find" should "successfully retrieve all matching permission tickets" in {
    permission
      .flatMap(_.find())
      .map(_ should not be empty)
      .shouldReturnSuccess
  }

  "findByScope" should "successfully return permission ticket matching the given scope" in {
    permission
      .flatMap(_.findByScope("PermissionScope"))
      .map(_ should not be empty)
      .shouldReturnSuccess
  }

  "findByResource" should "successfully return permission ticket matching the given resource" in {
    permission
      .flatMap(_.findByResource(resourceId.get()))
      .map(_ should not be empty)
      .shouldReturnSuccess
  }

  "delete" should "successfully delete the specified permission ticket" in {
    permission
      .flatMap(_.delete(ticket.get().id))
      .shouldReturnSuccess
  }

  "deleteRealm" should "delete test realm" in {
    realmService
      .delete("AuthzRealm")
      .shouldReturnSuccess
  }
}
