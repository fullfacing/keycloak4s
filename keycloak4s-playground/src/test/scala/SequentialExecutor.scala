import suites._
import org.scalatest.Sequential

class SequentialExecutor extends Sequential(
  new AuthenticationManagementTests,
  new ClientsTests,
  new ComponentsTests,
  new GroupsTests,
  new IdentityProvidersTests,
  new RealmsTests,
  new RolesTests,
  new RolesByIdTests,
  new ScopeMappingsTests,
  new UsersTests
)
