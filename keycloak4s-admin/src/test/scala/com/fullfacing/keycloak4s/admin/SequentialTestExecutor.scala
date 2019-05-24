package com.fullfacing.keycloak4s.admin

import com.fullfacing.keycloak4s.admin.tests.{RolesTests, UsersTests}
import org.scalatest.Sequential

class SequentialTestExecutor extends Sequential(
  new RolesTests,
  new UsersTests
)
