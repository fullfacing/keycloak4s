package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class TokenType(val value: String) extends StringEnumEntry

case object TokenTypes extends StringEnum[TokenType] {
  case object Access  extends TokenType("Access Token")
  case object Id      extends TokenType("ID Token")
  case object Unknown extends TokenType("Bearer Token")

  def values: immutable.IndexedSeq[TokenType] = findValues
}
