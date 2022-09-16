package suites

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import cats.data.EitherT
import cats.effect.IO
import com.fullfacing.keycloak4s.core.models.Component
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

@DoNotDiscover
class ComponentsTests extends IntegrationSpec {

  /**
   * Calls in this service not covered by the below tests:
   *
   * fetchSubComponentTypes  - Unable to get back valid response.
   */

  val create = Component.Create(
    name = Some("TestComponent"),
    providerId = "aes-generated",
    providerType = "org.keycloak.keys.KeyProvider"
  )

  val component = new AtomicReference[UUID]()

  "create" should "successfully create a component for the realm" in {
    val IO =
      for {
        _ <- EitherT(componentService.create(create))
        l <- EitherT(componentService.fetch(name = Some("TestComponent"), None, None))
        c <- EitherT.fromOption[IO](l.headOption, Errors.COMPONENT_NOT_FOUND)
      } yield component.set(c.id)

    IO.value.shouldReturnSuccess
  }

  "fetch" should "successfully retrieve all components in the realm" in {
    componentService.fetch(None, None, None).shouldReturnSuccess
  }

  "fetchById" should "successfully retrieve the " in {
    val IO = EitherT(componentService.fetchById(component.get())).map { c =>
      c.name shouldBe Some("TestComponent")
      c.id   shouldBe component.get()
    }

    IO.value.shouldReturnSuccess
  }

  "update" should "successfully update one or more properties of the component object" in {
    val update = Component.Update(
      name         = Some("Test"),
      providerId   = Some("allowed-protocol-mappers"),
      providerType = Some("org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy")
    )

    val IO =
      for {
        _ <- EitherT(componentService.update(component.get(), update))
        c <- EitherT(componentService.fetchById(component.get()))
      } yield {
        c.name         shouldBe update.name
        c.providerId   shouldBe update.providerId.get
        c.providerType shouldBe update.providerType.get
      }

    IO.value.shouldReturnSuccess
  }

  /*"fetchSubComponentTypes" should "retrieve all component sub types for this component" in {
    componentService.fetchSubComponentTypes(component.get())
      .shouldReturnSuccess
  }*/

  "delete" should "successfully delete the component from the realm" in {
    val IO =
      for {
        _ <- EitherT(componentService.delete(component.get()))
        l <- EitherT(componentService.fetch(None, None, None))
      } yield {
        l.exists(_.name.contains("TestComponent")) shouldNot be (true)
      }

    IO.value.shouldReturnSuccess
  }


}