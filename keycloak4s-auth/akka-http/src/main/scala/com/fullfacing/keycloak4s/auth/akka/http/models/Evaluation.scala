package com.fullfacing.keycloak4s.auth.akka.http.models

sealed trait Evaluation[A]

final case class Result[A](r: Boolean) extends Evaluation[A]
final case class Continue[A](a: A) extends Evaluation[A]
