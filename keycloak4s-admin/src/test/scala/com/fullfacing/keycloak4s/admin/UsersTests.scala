package com.fullfacing.keycloak4s.admin

import cats.effect.IO
import com.fullfacing.keycloak4s.admin.client.Keycloak
import com.fullfacing.keycloak4s.admin.services.Users

class UsersTests extends TestBase {

  val service: Users[IO, Nothing] = Keycloak.Users[IO, Nothing]

  "fetch" should "successfully return a sequence of User models" in {
    service.fetch().map(isSuccessful).unsafeToFuture()
  }
}