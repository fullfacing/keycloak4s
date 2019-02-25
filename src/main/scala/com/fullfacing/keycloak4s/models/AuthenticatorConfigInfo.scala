package models

case class AuthenticatorConfigInfo(
                                    helpText: Option[String],
                                    name: Option[String],
                                    properties: Option[List[ConfigProperty[_]]],
                                    providerId: Option[String],
                                  )