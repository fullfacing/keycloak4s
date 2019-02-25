package com.fullfacing.keycloak4s.models

case class Mappings(
                     Mappings: Option[Map[_, _]],
                     realmMappings: Option[List[Role]]
                   )
