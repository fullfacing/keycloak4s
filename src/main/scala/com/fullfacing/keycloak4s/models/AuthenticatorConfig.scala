package models

case class AuthenticatorConfig(
                                alias: Option[String],
                                config: Option[Map[_, _]],
                                id: Option[String]
                              )
