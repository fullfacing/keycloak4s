import suites._
import org.scalatest.Sequential

class IntegrationTests extends Sequential(
  new AttackDetectionTests,
  new AuthenticationManagementTests,
  new ClientsTests,
  new ComponentsTests,
  new GroupsTests,
  new IdentityProvidersTests,
  new ProtocolMapperTests,
  new RealmsTests,
  new RolesByIdTests,
  new RolesTests,
  new ClientScopeTests,
  new UsersTests
)
