package com.fullfacing.keycloak4s.core.models

final case class KeysMetadata(active: Option[Map[String, String]],
                              keys: Option[List[KeysMetadata.Key]])

object KeysMetadata {
  final case class Key(algorithm: Option[String],
                       certificate: Option[String],
                       kid: Option[String],
                       providerId: Option[String],
                       providerPriority: Option[Long],
                       publicKey: Option[String],
                       status: Option[String],
                       `type`: Option[String])
}
