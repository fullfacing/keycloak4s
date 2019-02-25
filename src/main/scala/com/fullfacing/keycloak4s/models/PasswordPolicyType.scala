package models

case class PasswordPolicyType(
                               configType: Option[String],
                               defaultValue: Option[String],
                               displayName: Option[String],
                               id: Option[String],
                               multipleSupported: Option[Boolean]
                             )
