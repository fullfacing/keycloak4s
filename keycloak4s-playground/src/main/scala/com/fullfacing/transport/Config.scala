package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.models.{NodeConfiguration, PathConfiguration}

import scala.io.Source

object Config {

  val config: String = {
    val a = getClass.getResource("/config.json")
    val b = Source.fromFile(a.getPath)
    b.mkString.stripMargin
  }
  val apiSecurityConfig: NodeConfiguration = NodeConfiguration(config)

  val clientsApiConfig: NodeConfiguration = {
    val a = getClass.getResource("/clients_configA.json")
    val b = Source.fromFile(a.getPath)
    NodeConfiguration(b.mkString.stripMargin)
  }

  val pathClientsConfig: PathConfiguration = {
    val a = getClass.getResource("/clients_configB.json")
    val b = Source.fromFile(a.getPath)
    PathConfiguration(b.mkString.stripMargin)
  }
}
