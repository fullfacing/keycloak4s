package com.fullfacing.keycloak4s.adapters.akka.http

case class AuthResult(permissions: List[String])

object TokenValidator {

  implicit val pk: List[String] = List.empty[String]

  def validate(rawToken: String)(implicit publicKeys: List[String]): Either[String, AuthResult] = {
    Right(AuthResult(List.empty))
  }
}
