package com.fullfacing.keycloak4s.admin.models

final case class KeysMetadata(active: Option[Map[String, String]],
                              keys: Option[List[KeysMetadataRepresentationKey]])
