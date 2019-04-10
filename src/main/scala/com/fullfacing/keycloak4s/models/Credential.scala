package com.fullfacing.keycloak4s.models

import com.fullfacing.keycloak4s.models.Credential.CredentialType
import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

final case class Credential(`type`: CredentialType,
                            value: String,
                            algorithm: Option[String],
                            config: Option[MultivaluedHashMap],
                            counter: Option[Int],
                            createdDate: Option[Long],
                            device: Option[String],
                            digits: Option[Int],
                            hashIterations: Option[Int],
                            hashedSaltedValue: Option[String],
                            period: Option[Int],
                            salt: Option[String],
                            temporary: Option[Boolean])

object Credential {

  sealed abstract class CredentialType(val value: String) extends StringEnumEntry
  case object CredentialTypes extends StringEnum[CredentialType] {
    case object Otp             extends CredentialType("otp")
    case object Totp            extends CredentialType("totp")
    case object Hotp            extends CredentialType("hotp")
    case object Secret          extends CredentialType("secret")
    case object ClientCert      extends CredentialType("cert")
    case object Kerberos        extends CredentialType("kerberos")
    case object Password        extends CredentialType("password")
    case object PasswordToken   extends CredentialType("password-token")
    case object PasswordHistory extends CredentialType("password-history")

    val values: immutable.IndexedSeq[CredentialType] = findValues
  }
}
