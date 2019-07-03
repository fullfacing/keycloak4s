package com.fullfacing.keycloak4s.auth.akka.http.models.path

trait AndOr

case class And(and: Either[AndOr, List[String]])

case class Or(or: Either[AndOr, List[String]])

