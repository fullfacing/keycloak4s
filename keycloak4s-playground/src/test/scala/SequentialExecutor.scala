import suites._
import org.scalatest.Sequential

class SequentialExecutor extends Sequential(
  new ClientsTests,
  new GroupsTests,
  new RealmsTests,
  new RolesTests,
  new RolesByIdTests,
  new UsersTests
)
