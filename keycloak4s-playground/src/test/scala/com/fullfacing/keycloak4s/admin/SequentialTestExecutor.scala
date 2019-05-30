package com.fullfacing.keycloak4s.admin

import com.fullfacing.keycloak4s.admin.tests._
import org.scalatest.Sequential

class SequentialTestExecutor extends Sequential(
  new GroupsTests,
  new RealmsTests,
  new RolesTests,
  new RolesByIdTests,
  new UsersTests
)
