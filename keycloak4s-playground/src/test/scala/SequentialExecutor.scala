import suites._
import org.scalatest.Sequential

class SequentialExecutor extends Sequential(
  new AttackDetectionTests,
  new ClientsTests,
  new ComponentsTests,
  new GroupsTests,
  new ProtocolMapperTests,
  new RealmsTests,
  new RolesTests,
  new RolesByIdTests,
  new ScopeMappingsTests,
  new UsersTests
)
