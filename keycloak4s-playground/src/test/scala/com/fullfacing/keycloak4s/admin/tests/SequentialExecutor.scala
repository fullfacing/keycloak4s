package com.fullfacing.keycloak4s.admin.tests

import com.fullfacing.keycloak4s.admin.tests.suites._
import org.scalatest.Sequential

class SequentialExecutor extends Sequential(
  new GroupsTests,
  new RealmsTests,
  new RolesTests,
  new RolesByIdTests,
  new UsersTests
)
