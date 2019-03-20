package com.fullfacing.keycloak4s.models

case class Mappings(Mappings: Option[Map[String, ClientMappings]],
                    realmMappings: Option[List[Role]])
