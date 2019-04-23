package com.fullfacing.keycloak4s.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

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
