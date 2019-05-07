package com.fullfacing.keycloak4s.admin.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class Protocol(val value: String) extends StringEnumEntry
case object Protocols extends StringEnum[Protocol] {
  case object Saml          extends Protocol("saml")
  case object OpenIdConnect extends Protocol("openid-connect")

  val values: immutable.IndexedSeq[Protocol] = findValues
}
