package com.fullfacing.keycloak4s.admin.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class RequiredAction(val value: String) extends StringEnumEntry

case object RequiredActions extends StringEnum[RequiredAction] {
  case object VerifyEmail        extends RequiredAction("VERIFY_EMAIL")
  case object ConfigureTotp      extends RequiredAction("CONFIGURE_TOTP")
  case object UpdateProfile      extends RequiredAction("UPDATE_PROFILE")
  case object UpdatePassword     extends RequiredAction("UPDATE_PASSWORD")
  case object TermsAndConditions extends RequiredAction("terms_and_conditions")

  val values: immutable.IndexedSeq[RequiredAction] = findValues
}
