package suites.authz

import com.fullfacing.keycloak4s.authz.monix.bio.models.PermissionTicket
import com.fullfacing.keycloak4s.core.models.Resource
import org.scalatest.DoNotDiscover
import suites.authz.IoIntegration._

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class PermissionTests extends IoIntegration {

  val saved: AtomicReference[PermissionTicket] = new AtomicReference[PermissionTicket]()
  val resourceId: AtomicReference[UUID] = new AtomicReference[UUID]()

  "request" should "return successfully" in {
    (for {
      r1 <- resource
      r2 <- r1.create(Resource.Create("PermissionResource", ownerManagedAccess = Some(true)))
      _  = resourceId.set(r2._id)
      p1 <- permission
      p2 <- p1.request(PermissionTicket.Request(r2._id.toString))
    } yield p2)
      .shouldReturnSuccess
  }

//  "create" should "successfully create a new permission ticket" in {
//    val create = PermissionTicket.Create(
//      requester = ???,
//      resource  = Some(resourceId.get().toString),
//      scope     = Some("")
//    )
//    permission
//      .flatMap(_.create(PermissionTicket.Create()))
//      .shouldReturnSuccess
//  }

//  "update" should "successfully update the given permission ticket" in {
//    permission
//      .flatMap(_.update(saved.get()))
//      .shouldReturnSuccess
//  }

  "find" should "successfully retrieve all matching permission tickets" in {
    permission
      .flatMap(_.find())
      .shouldReturnSuccess
  }

  "findByScope" should "successfully return permission ticket matching the given scope" in {
    permission
      .flatMap(_.findByScope(""))
      .shouldReturnSuccess
  }

  "findByResource" should "successfully return permission ticket matching the given resource" in {
    permission
      .flatMap(_.findByResource(""))
      .shouldReturnSuccess
  }

//  "delete" should "successfully delete the specified permission ticket" in {
//    permission
//      .flatMap(_.delete(saved.get().id))
//      .shouldReturnSuccess
//  }

  "deleteRealm" should "delete test realm" in {
    realmService
      .delete("AuthzRealm")
      .shouldReturnSuccess
  }
}
