package com.fullfacing.keycloak4s.auth.akka.http.models

trait Evaluation[A]

case class Result[A](r: Boolean) extends Evaluation[A]
case class Continue[A](a: A) extends Evaluation[A]
