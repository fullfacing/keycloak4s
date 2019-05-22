package com.fullfacing.keycloak4s.admin.tests

import cats.effect.IO
import com.fullfacing.keycloak4s.admin.TestBase
import com.fullfacing.keycloak4s.admin.client.Keycloak
import com.fullfacing.keycloak4s.admin.services.RealmsAdmin

class RealmsTests extends TestBase {

  val service: RealmsAdmin[IO, Nothing] = Keycloak.RealmsAdmin[IO, Nothing]

  "fetch" should "successfully return a sequence of User models" in {
    service.fetchTopLevelRepresentation().map(isSuccessful).unsafeToFuture()
  }
}
