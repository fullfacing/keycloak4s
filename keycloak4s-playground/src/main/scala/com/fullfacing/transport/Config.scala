package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.models.{NodeConfiguration, PathConfiguration, PathMethodRoles, PathRoles}
import com.fullfacing.keycloak4s.core.models.enums.Methods

import scala.io.Source

object Config {

  val config: String = {
    val a = getClass.getResource("/config.json")
    val b = Source.fromFile(a.getPath)
    b.mkString.stripMargin
  }
  val apiSecurityConfig: NodeConfiguration = NodeConfiguration(config)

  val clientsApiConfig: NodeConfiguration = {
    val a = getClass.getResource("/clients_config.json")
    val b = Source.fromFile(a.getPath)
    NodeConfiguration(b.mkString.stripMargin)
  }

  val pathClientsConfig: PathConfiguration = {
    val client = PathRoles(
      path = List("clients"),
      roles = List(
        PathMethodRoles(Methods.Get, List(List("client-view", "client-write", "client-delete"))),
        PathMethodRoles(Methods.Post, List(List("client-write", "client-delete"))),
        PathMethodRoles(Methods.Patch, List(List("client-write", "client-delete"))),
        PathMethodRoles(Methods.Delete, List(List("client-delete"))),
      )
    )

    val admin = PathRoles(
      path = List("*"),
      roles = List(PathMethodRoles(Methods.All, List(List("admin"))))
    )

    val cAccounts = PathRoles(
      path = List("clients", "accounts"),
      roles = List(PathMethodRoles(Methods.Get, List(List("client-view", "client-write", "client-delete"), List("account-view", "account-write", "account-delete"))))
    )
    import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
    import org.json4s.jackson.Serialization.write

    val top = PathConfiguration(
      service = "api-client",
      paths = List(client, admin, cAccounts)
    )
    println(write(top))
    top
  }
}
