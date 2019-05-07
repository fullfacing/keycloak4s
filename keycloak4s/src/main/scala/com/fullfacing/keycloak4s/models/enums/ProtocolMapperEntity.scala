package com.fullfacing.keycloak4s.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class ProtocolMapperEntity(val value: String) extends StringEnumEntry

case object ProtocolMapperEntities extends StringEnum[ProtocolMapperEntity] {
  case object Client extends ProtocolMapperEntity("clients")
  case object Scope  extends ProtocolMapperEntity("client-scopes")

  val values: immutable.IndexedSeq[ProtocolMapperEntity] = findValues
}
