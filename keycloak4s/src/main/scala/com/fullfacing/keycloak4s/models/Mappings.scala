package com.fullfacing.keycloak4s.models

final case class Mappings(clientMappings: Option[Map[String, ClientMappings]],
                          realmMappings: Option[List[Role]])
