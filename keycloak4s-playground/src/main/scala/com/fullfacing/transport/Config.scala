package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.models.SecurityConfig

import scala.io.Source

object Config {

  val config: String = {
    val a = getClass.getResource("/config.json")
    val b = Source.fromFile(a.getPath)
    b.mkString.stripMargin
  }
  val apiSecurityConfig: SecurityConfig = SecurityConfig(config)
}
