package com.fullfacing.keycloak4s.core.models

final case class KeyStoreConfig(format: Option[String],
                                keyAlias: Option[String],
                                keyPassword: Option[String],
                                realmAlias: Option[String],
                                realmCertificate: Option[Boolean],
                                storePassword: Option[String])