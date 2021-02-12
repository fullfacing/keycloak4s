package suites.authz

import com.fullfacing.keycloak4s.authz.monix.bio.models.UmaPermission
import com.fullfacing.keycloak4s.core.models.Resource
import org.scalatest.DoNotDiscover
import suites.authz.IoIntegration._

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@DoNotDiscover
class PolicyTests extends IoIntegration {

  val saved: AtomicReference[UmaPermission] = new AtomicReference[UmaPermission]()
  val resourceId: AtomicReference[UUID] = new AtomicReference[UUID]()

  "create" should "successfully create a new policy" in {
    (for {
      r1 <- resource
      r2 <- r1.create(Resource.Create("PolicyResource", ownerManagedAccess = Some(true)))
      _  = resourceId.set(r2._id)
      p1 <- policy
      p2 <- p1.create(resourceId.get(), UmaPermission.Create("Test"))
    } yield saved.set(p2))
      .shouldReturnSuccess
  }

  "find" should "should successfully return all policies" in {
    policy
      .flatMap(_.find(resource = Option(resourceId.get().toString)))
      .shouldReturnSuccess
  }

  "update" should "update the specified policy and return a success value" in {
    policy
      .flatMap(_.update(saved.get.copy(description = Some("New Description"))))
      .shouldReturnSuccess
  }

  "findById" should "should successfully return the specified policy" in {
    policy
      .flatMap(_.findById(saved.get().id))
      .shouldReturnSuccess
  }

  "delete" should "successfully delete the specified policy" in {
    policy
      .flatMap(_.delete(saved.get().id))
      .shouldReturnSuccess
  }
}
