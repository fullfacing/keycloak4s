package models

case class KeyStoreConfig(
                           format: Option[String],
                           keyAlias: Option[String],
                           keyPassword: Option[String],
                           realmAlias: Option[String],
                           realmCertificate: Option[Boolean],
                           storePassword: Option[String]

                         )