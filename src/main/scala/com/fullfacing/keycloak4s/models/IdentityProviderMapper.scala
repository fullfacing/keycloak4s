package models

case class IdentityProviderMapper(
                                   config: Option[Map[_, _]],
                                   id: Option[String],
                                   identityProviderAlias: Option[String],
                                   identityProviderMapper: Option[String],
                                   name: Option[String]

                                 )
