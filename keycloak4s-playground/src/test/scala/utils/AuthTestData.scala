package utils

import com.fullfacing.keycloak4s.auth.akka.http.models.path.{And, Or, PathMethodRoles, PathRule}
import com.fullfacing.keycloak4s.core.models.enums.Methods

object AuthTestData {

  object config2 {

    val resource1MethodRoles: List[PathMethodRoles] = List(
      PathMethodRoles(
        method = Methods.Get,
        roles  = And(List(Left(Or(List(Right("read1"), Right("write1"), Right("delete1"))))))
      ),
      PathMethodRoles(
        method = Methods.Delete,
        roles  = And(List(Left(Or(List(Right("delete1"))))))
      ),
      PathMethodRoles(
        method = Methods.Post,
        roles  = And(List(Left(Or(List(Right("write1"), Right("delete1"))))))
      )
    )

    val resource2MethodRoles: List[PathMethodRoles] = List(
      PathMethodRoles(
        method = Methods.Get,
        roles  = And(List(Left(Or(List(Right("read2"), Right("write2"), Right("delete2"))))))
      ),
      PathMethodRoles(
        method = Methods.Delete,
        roles  = And(List(Left(Or(List(Right("delete2"))))))
      ),
      PathMethodRoles(
        method = Methods.Post,
        roles  = And(List(Left(Or(List(Right("write2"), Right("delete2"))))))
      )
    )

    val resourceMergeMethodRoles: List[PathMethodRoles] = List(
      PathMethodRoles(
        method = Methods.Get,
        roles  = And(List(
          Left(Or(List(Right("read1"), Right("write1"), Right("delete1")))),
          Left(Or(List(Right("read2"), Right("write2"), Right("delete2"))))
        ))
      ),
      PathMethodRoles(
        method = Methods.Delete,
        roles  = And(List(
          Left(Or(List(Right("delete1")))),
          Left(Or(List(Right("delete2"))))
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

    val paths: List[PathRule] = List(path1, path2, path3, path4)
  }
}
