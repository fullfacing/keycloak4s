package com.fullfacing.keycloak4s.admin.models

final case class Mappings(clientMappings: Map[String, ClientMappings] = Map.empty,
                          realmMappings: List[Role])
