package com.fullfacing.keycloak4s.admin

import cats.data.EitherT
import cats.effect.IO
import com.fullfacing.keycloak4s.admin.client.Keycloak
import com.fullfacing.keycloak4s.admin.services.Roles
import com.fullfacing.keycloak4s.core.models.Role
import org.json4s.jackson.Serialization.writePretty
import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default

class RolesTests extends TestBase {

  val service: Roles[IO, Nothing] = Keycloak.Roles[IO, Nothing]
  private val realmRoleService = service.RealmLevel
  private val clientRoleService = service.RealmLevel

  val realmRole1: Role.Create = Role.Create(
    clientRole = false,
    composite  = false,
    name       = "realmRole1"
  )

  val realmRole2: Role.Create = Role.Create(
    clientRole = false,
    composite  = false,
    name       = "realmRole2"
  )

  val clientRole1: Role.Create = Role.Create(
    clientRole = true,
    composite  = false,
    name       = "clientRole1"
  )

  val clientRole2: Role.Create = Role.Create(
    clientRole = true,
    composite  = false,
    name       = "clientRole2"
  )

  "Realm Level Role CRUD" should "Successfully create, read, update and delete a role" in {
    val task =
      (for {
        _ <- EitherT(realmRoleService.create(realmRole1))
        _ <- EitherT(realmRoleService.fetch())
        r <- EitherT(realmRoleService.fetchByName(realmRole1.name))
        _ <- EitherT(realmRoleService.update(r.name, Role.Update(name = r.name, description = Some("This is the description"))))
        _ <- EitherT(realmRoleService.fetchByName(r.name)).map(println)
        _ <- EitherT(realmRoleService.remove(r.name))
      } yield ()).value

    task.map(isSuccessful).unsafeToFuture()
  }

  "Client Level Role CRUD" should "Successfully create, read, update and delete a role" in {
    val task =
      (for {
        _ <- EitherT(clientRoleService.create(clientRole1))
        _ <- EitherT(clientRoleService.fetch())
        r <- EitherT(clientRoleService.fetchByName(clientRole1.name))
        _ <- EitherT(clientRoleService.update(r.name, Role.Update(name = r.name, description = Some("This is the description"))))
        _ <- EitherT(clientRoleService.fetchByName(r.name)).map(println)
        _ <- EitherT(clientRoleService.remove(r.name))
      } yield ()).value

    task.map(isSuccessful).unsafeToFuture()
  }


}
