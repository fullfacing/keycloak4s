import suites._
import org.scalatest.Sequential
import suites.authz.{AuthzTests, PermissionTests, PolicyTests, ProtectedResourceTests}

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
  new UsersTests,
  new AuthzTests,
  new ProtectedResourceTests,
  new PolicyTests,
  new PermissionTests
)
