package com.fullfacing.keycloak4s.core.models

final case class KeysMetadata(active: Option[Map[String, String]],
                              keys: Option[List[KeysMetadataRepresentationKey]])
