package com.fullfacing.keycloak4s.admin.tests

import cats.effect.IO
import com.fullfacing.keycloak4s.admin.IntegrationSpec
import com.fullfacing.keycloak4s.admin.client.Keycloak
import com.fullfacing.keycloak4s.admin.services.RealmsAdmin
import org.scalatest.DoNotDiscover

@DoNotDiscover
class RealmsTests extends IntegrationSpec {

  val service: RealmsAdmin[IO, Nothing] = Keycloak.RealmsAdmin[IO, Nothing]

  "fetch" should "successfully return a sequence of User models" in {
    service.fetchTopLevelRepresentation().map(isSuccessful).unsafeToFuture()
  }
}
