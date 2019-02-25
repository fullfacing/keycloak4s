package models

case class KeysMetadataRepresentationKey(
                                                                algorithm: Option[String],
                                                                certificate: Option[String],
                                                                kid: Option[String],
                                                                providerId: Option[String],
                                                                providerPriority: Option[Long],
                                                                publicKey: Option[String],
                                                                status: Option[String],
                                                                `type`: Option[String]
                                                              )
