package utils

import com.fullfacing.keycloak4s.auth.core.models.path.{And, Or, PathMethodRoles, PathRule}
import com.fullfacing.keycloak4s.auth.core.validation.TokenValidator
import com.fullfacing.keycloak4s.core.models.ConfigWithoutAuth
import com.fullfacing.keycloak4s.core.models.enums.Methods

object AuthTestData {

  val scheme  = "http"
  val host    = "localhost"
  val port    = 8080
  val realm   = "test"

  val keycloakConfig = ConfigWithoutAuth(scheme, host, port, realm)

  implicit val validator: TokenValidator = TokenValidator.Static(TestData.jwkSet, keycloakConfig)
  val validatorUri = s"$scheme://$host:$port/auth/realms/$realm"

  object config2 {

    val resource1MethodRoles: List[PathMethodRoles] = List(
      PathMethodRoles(
        method = Methods.Delete,
        roles  = Or(List(Right("delete1")))
      ),
      PathMethodRoles(
        method = Methods.Get,
        roles  = Or(List(Right("read1"), Right("write1"), Right("delete1")))
      ),
      PathMethodRoles(
        method = Methods.Post,
        roles  = Or(List(Right("write1"), Right("delete1")))
      )
    )

    val resource2MethodRoles: List[PathMethodRoles] = List(
      PathMethodRoles(
        method = Methods.Delete,
        roles  = Or(List(Right("delete2")))
      ),
      PathMethodRoles(
        method = Methods.Get,
        roles  = Or(List(Right("read2"), Right("write2"), Right("delete2")))
      ),
      PathMethodRoles(
        method = Methods.Post,
        roles  = Or(List(Right("write2"), Right("delete2")))
      )
    )

    val resourceMergeMethodRoles: List[PathMethodRoles] = List(
      PathMethodRoles(
        method = Methods.Delete,
        roles  = And(List(
          Left(Or(List(Right("delete1")))),
          Left(Or(List(Right("delete2"))))
        ))
      ),
      PathMethodRoles(
        method = Methods.Get,
        roles  = And(List(
          Left(Or(List(Right("read1"), Right("write1"), Right("delete1")))),
          Left(Or(List(Right("read2"), Right("write2"), Right("delete2"))))
        ))
      ),
      PathMethodRoles(
        method = Methods.Post,
        roles  = And(List(
          Left(Or(List(Right("write1"), Right("delete1")))),
          Left(Or(List(Right("write2"), Right("delete2"))))
        ))
      )
    )

    val resourceActionMethodRoles: List[PathMethodRoles] = List(
      PathMethodRoles(
        method = Methods.Delete,
        roles  = And(List(
          Left(Or(List(Right("delete1")))),
          Left(Or(List(Right("delete2"))))
        ))
      ),
      PathMethodRoles(
        method = Methods.Get,
        roles  = And(List(
          Left(And(List(
            Left(Or(List(Right("read1"), Right("write1"), Right("delete1")))),
            Left(Or(List(Right("read2"), Right("write2"), Right("delete2"))))
          ))),
          Left(And(List(Right("action-get")))),
        ))
      ),
      PathMethodRoles(
        method = Methods.Head,
        roles  = And(List(Right("action-head")))
      ),
      PathMethodRoles(
        method = Methods.Post,
        roles  = And(List(
          Left(Or(List(Right("write1"), Right("delete1")))),
          Left(Or(List(Right("write2"), Right("delete2"))))
        ))
      )
    )

    val path1: PathRule = PathRule(
      path = List("v1", "resource1"),
      methodRoles = resource1MethodRoles
    )

    val path2: PathRule = PathRule(
      path = List("v1", "resource1", "{id}"),
      methodRoles = resource1MethodRoles
    )

    val path3: PathRule = PathRule(
      path = List("v1", "resource1", "{id}", "resource2"),
      methodRoles = resourceMergeMethodRoles
    )

    val path4: PathRule = PathRule(
      path = List("v1", "resource1", "{id}", "resource2", "{id}"),
      methodRoles = resourceMergeMethodRoles
    )

    val path5: PathRule = PathRule(
      path = List("v1", "resource1", "{id}", "resource2", "action"),
      methodRoles = resourceActionMethodRoles
    )

    val paths: List[PathRule] = List(path1, path2, path3, path4, path5)
  }
}
