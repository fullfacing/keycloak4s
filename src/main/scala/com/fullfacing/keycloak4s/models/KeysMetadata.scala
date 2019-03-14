package com.fullfacing.keycloak4s.models

case class KeysMetadata(active: Option[Map[String, String]],
                        keys: Option[List[KeysMetadataRepresentationKey]])
