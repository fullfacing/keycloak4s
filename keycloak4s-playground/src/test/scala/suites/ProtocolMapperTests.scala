package suites

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import cats.data.EitherT
import com.fullfacing.keycloak4s.core.models.enums.{ProtocolMapperEntities, Protocols}
import com.fullfacing.keycloak4s.core.models.{Client, ClientScope, ProtocolMapper}
import monix.eval.Task
import org.scalatest.DoNotDiscover
import utils.{Errors, IntegrationSpec}

@DoNotDiscover
class ProtocolMapperTests extends IntegrationSpec {

  val clientCreate = Client.Create("protocol-mappers-test")
  val scope1Create = ClientScope.Create(name = "scope1")
  val create1      = ProtocolMapper.Create(name = "ProtocolMap1", protocol = Protocols.OpenIdConnect, protocolMapper = "oidc-usermodel-realm-role-mapper", consentRequired = true)
  val create2      = ProtocolMapper.Create(name = "ProtocolMap2", protocol = Protocols.OpenIdConnect, protocolMapper = "oidc-usermodel-realm-role-mapper", consentRequired = true)
  val create3      = ProtocolMapper.Create(name = "ProtocolMap3", protocol = Protocols.Saml, protocolMapper = "saml-user-session-note-mapper", consentRequired = true)
  val create4      = ProtocolMapper.Create(name = "ProtocolMap4", protocol = Protocols.Saml, protocolMapper = "saml-user-session-note-mapper", consentRequired = true)

  private val clientUuid    = new AtomicReference[UUID]()
  private val scope1        = new AtomicReference[UUID]()
  private val cProtocolMap1  = new AtomicReference[UUID]()
  private val sProtocolMap1  = new AtomicReference[UUID]()
  private val cProtocolMap2  = new AtomicReference[UUID]()
  private val sProtocolMap2  = new AtomicReference[UUID]()
  private val cProtocolMap3  = new AtomicReference[UUID]()
  private val sProtocolMap3  = new AtomicReference[UUID]()
  private val cProtocolMap4  = new AtomicReference[UUID]()
  private val sProtocolMap4  = new AtomicReference[UUID]()

  "Create Supporting objects" should "" in {
    val task =
      for {
        _  <- EitherT(clientService.create(clientCreate))
        c  <- EitherT(clientService.fetch(clientId = Some("protocol-mappers-test")))
        c1 <- EitherT.fromOption[Task](c.headOption, Errors.CLIENT_NOT_FOUND)
        _  =  clientUuid.set(c1.id)
        _  <- EitherT(clientScopeService.create(scope1Create))
        cs <- EitherT(clientScopeService.fetch())
        s1 <- EitherT.fromOption[Task](cs.find(_.name == "scope1"), Errors.SCOPE_NOT_FOUND)
      } yield scope1.set(s1.id)

      task.value.shouldReturnSuccess
  }

  "create" should "add a protocol mapper to the client or client scope" in {
    val task =
      for {
        _ <- EitherT(protocolMapService.create(clientUuid.get(), ProtocolMapperEntities.Client, create1))
        _ <- EitherT(protocolMapService.create(scope1.get(), ProtocolMapperEntities.Scope, create1))
        _ <- EitherT(protocolMapService.create(clientUuid.get(), ProtocolMapperEntities.Client, create2))
        _ <- EitherT(protocolMapService.create(scope1.get(), ProtocolMapperEntities.Scope, create2))
        c <- EitherT(protocolMapService.fetch(clientUuid.get(), ProtocolMapperEntities.Client))
        s <- EitherT(protocolMapService.fetch(scope1.get(), ProtocolMapperEntities.Scope))
      } yield {
        val p1 = c.find(_.name == create1.name)
        val p2 = s.find(_.name == create1.name)
        val p3 = c.find(_.name == create2.name)
        val p4 = s.find(_.name == create2.name)

        p1.nonEmpty shouldBe true
        p2.nonEmpty shouldBe true
        p3.nonEmpty shouldBe true
        p4.nonEmpty shouldBe true

        cProtocolMap1.set(p1.get.id)
        sProtocolMap1.set(p2.get.id)
        cProtocolMap2.set(p3.get.id)
        sProtocolMap2.set(p4.get.id)
      }

    task.value.shouldReturnSuccess
  }

  "createMany" should "add the given valid protocol mappers to the client or client scope" in {
    val task =
      for {
        _ <- EitherT(protocolMapService.createMany(clientUuid.get(), ProtocolMapperEntities.Client, List(create3, create4)))
        _ <- EitherT(protocolMapService.createMany(scope1.get(), ProtocolMapperEntities.Scope, List(create3, create4)))
        c <- EitherT(protocolMapService.fetch(clientUuid.get(), ProtocolMapperEntities.Client))
        s <- EitherT(protocolMapService.fetch(scope1.get(), ProtocolMapperEntities.Scope))
      } yield {
        val p1 = c.find(_.name == create3.name)
        val p2 = s.find(_.name == create3.name)
        val p3 = c.find(_.name == create4.name)
        val p4 = s.find(_.name == create4.name)

        p1.nonEmpty shouldBe true
        p2.nonEmpty shouldBe true
        p3.nonEmpty shouldBe true
        p4.nonEmpty shouldBe true

        cProtocolMap3.set(p1.get.id)
        sProtocolMap3.set(p2.get.id)
        cProtocolMap4.set(p3.get.id)
        sProtocolMap4.set(p4.get.id)
      }


    task.value.shouldReturnSuccess
  }

  "fetch" should "retrieve all protocol mappers belonging to the given client or scope" in {
    val task =
      for {
        c <- EitherT(protocolMapService.fetch(clientUuid.get(), ProtocolMapperEntities.Client))
        s <- EitherT(protocolMapService.fetch(scope1.get(), ProtocolMapperEntities.Scope))
      } yield {
        c.nonEmpty shouldBe true
        s.nonEmpty shouldBe true
      }

    task.value.shouldReturnSuccess
  }

  "fetchById" should "retrieve the protocol mapper with the given" in {
    val task =
      for {
        _ <- protocolMapService.fetchById(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap1.get())
        r <- protocolMapService.fetchById(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap1.get())
      } yield r

    task.shouldReturnSuccess
  }

  "fetchByProtocol" should "retrieve all protocol mappers belong to the given entity and have the given protocol type" in {
    val task =
      for {
        c1 <- EitherT(protocolMapService.fetchByProtocol(clientUuid.get(), ProtocolMapperEntities.Client, Protocols.OpenIdConnect))
        s1 <- EitherT(protocolMapService.fetchByProtocol(scope1.get(), ProtocolMapperEntities.Scope, Protocols.OpenIdConnect))
        c2 <- EitherT(protocolMapService.fetchByProtocol(clientUuid.get(), ProtocolMapperEntities.Client, Protocols.Saml))
        s2 <- EitherT(protocolMapService.fetchByProtocol(scope1.get(), ProtocolMapperEntities.Scope, Protocols.Saml))
      } yield {
        c1.exists(_.id == cProtocolMap1.get()) shouldBe true
        c1.exists(_.id == cProtocolMap2.get()) shouldBe true

        s1.exists(_.id == sProtocolMap1.get()) shouldBe true
        s1.exists(_.id == sProtocolMap1.get()) shouldBe true

        c2.exists(_.id == cProtocolMap3.get()) shouldBe true
        c2.exists(_.id == cProtocolMap4.get()) shouldBe true

        s2.exists(_.id == sProtocolMap3.get()) shouldBe true
        s2.exists(_.id == sProtocolMap4.get()) shouldBe true
      }
    task.value.shouldReturnSuccess
  }

  "update" should "successfully alter the specified properties of the given protocol mapper" in {
    val update = (id: UUID, c: ProtocolMapper.Create) =>
      ProtocolMapper.Update(
        id             = id,
        protocol       = c.protocol,
        protocolMapper = c.protocolMapper,
        config         = Some(Map("key" -> "value"))
      )

    val task =
      for {
        _  <- EitherT(protocolMapService.update(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap1.get(), update(cProtocolMap1.get(), create1)))
        _  <- EitherT(protocolMapService.update(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap1.get(), update(sProtocolMap1.get(), create1)))
        _  <- EitherT(protocolMapService.update(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap3.get(), update(cProtocolMap3.get(), create3)))
        _  <- EitherT(protocolMapService.update(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap3.get(), update(sProtocolMap3.get(), create3)))
        c1 <- EitherT(protocolMapService.fetchById(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap1.get()))
        s1 <- EitherT(protocolMapService.fetchById(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap1.get()))
        c2 <- EitherT(protocolMapService.fetchById(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap3.get()))
        s2 <- EitherT(protocolMapService.fetchById(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap3.get()))
      } yield {
        c1.config.exists(_ == ("key" -> "value")) shouldBe true
        s1.config.exists(_ == ("key" -> "value")) shouldBe true
        c2.config.exists(_ == ("key" -> "value")) shouldBe true
        s2.config.exists(_ == ("key" -> "value")) shouldBe true
      }

    task.value.shouldReturnSuccess
  }

  "delete" should "remove the protocol mapper from the given client or scope" in {
    val task =
      for {
        _ <- EitherT(protocolMapService.delete(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap1.get()))
        _ <- EitherT(protocolMapService.delete(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap2.get()))
        _ <- EitherT(protocolMapService.delete(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap3.get()))
        _ <- EitherT(protocolMapService.delete(clientUuid.get(), ProtocolMapperEntities.Client, cProtocolMap4.get()))
        _ <- EitherT(protocolMapService.delete(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap1.get()))
        _ <- EitherT(protocolMapService.delete(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap2.get()))
        _ <- EitherT(protocolMapService.delete(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap3.get()))
        _ <- EitherT(protocolMapService.delete(scope1.get(), ProtocolMapperEntities.Scope, sProtocolMap4.get()))
        c <- EitherT(protocolMapService.fetch(clientUuid.get(), ProtocolMapperEntities.Client))
        s <- EitherT(protocolMapService.fetch(scope1.get(), ProtocolMapperEntities.Scope))
      } yield {
        c.isEmpty shouldBe true
        s.isEmpty shouldBe true
      }

    task.value.shouldReturnSuccess
  }

  "Delete Supporting Objects" should "remove all the ancillary objects created for testing Roles" in {
    val task =
      for {
        _ <- clientService.delete(clientUuid.get())
        r <- clientScopeService.delete(scope1.get())
      } yield r

    task.shouldReturnSuccess
  }
}
