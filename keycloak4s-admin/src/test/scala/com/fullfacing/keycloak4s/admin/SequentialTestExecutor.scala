package com.fullfacing.keycloak4s.admin

import com.fullfacing.keycloak4s.admin.tests.{RealmsTests, RolesTests, UsersTests}
import org.scalatest.Sequential

class SequentialTestExecutor extends Sequential(
  new RealmsTests,
  new RolesTests,
  new UsersTests
)
