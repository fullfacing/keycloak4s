package suites.authz

import com.fullfacing.keycloak4s.core.models.Resource
import org.scalatest.DoNotDiscover
import suites.authz.IoIntegration._

import java.util.concurrent.atomic.AtomicReference
@DoNotDiscover
class ProtectedResourceTests() extends IoIntegration {

  val saved: AtomicReference[Resource] = new AtomicReference()

  "create" should "successfully create a new protected resource" in {
    resource
      .flatMap(_.create(Resource.Create("TestProtectedResource")))
      .map(saved.set)
      .shouldReturnSuccess
  }

  "update" should "successfully update the given resource" in {
    resource
      .flatMap(_.update(saved.get()._id, saved.get().copy(displayName = Some("New Display Name"))))
      .shouldReturnSuccess
  }

  "findIds" should "successfully return the IDs of all resources" in {
    resource
      .flatMap(_.findIds())
      .shouldReturnSuccess
  }

  "find" should "successfully return all resources" in {
    resource
      .flatMap(_.find())
      .shouldReturnSuccess
  }

  "delete" should "successfully delete the specified resource" in {
    resource
      .flatMap(_.delete(saved.get()._id))
      .shouldReturnSuccess
  }
}
