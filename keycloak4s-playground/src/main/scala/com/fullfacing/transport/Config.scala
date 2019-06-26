package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.authorisation.{NodeAuthorisation, PathAuthorisation}

import scala.io.Source

object Config {

  lazy val config: String = {
    val a = getClass.getResource("/config.json")
    val b = Source.fromFile(a.getPath)
    b.mkString.stripMargin
  }
  lazy val apiSecurityConfig: NodeAuthorisation = NodeAuthorisation(config)

  lazy val nodeClientsConfig: NodeAuthorisation = {
    val a = getClass.getResource("/clients_configA.json")
    val b = Source.fromFile(a.getPath)
    NodeAuthorisation(b.mkString.stripMargin)
  }

  val pathClientsConfig: PathAuthorisation = {
    val a = getClass.getResource("/clients_configB.json")
    val b = Source.fromFile(a.getPath)
    PathAuthorisation(b.mkString.stripMargin)
  }
}
