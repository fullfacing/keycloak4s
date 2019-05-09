package com.fullfacing.keycloak4s.auth.akka.http.models

case class ResourceNode(resource: String,
                        nodes: List[ResourceNode] = List.empty)

case class SecurityConfig(service: String,
                          nodes: List[ResourceNode])
