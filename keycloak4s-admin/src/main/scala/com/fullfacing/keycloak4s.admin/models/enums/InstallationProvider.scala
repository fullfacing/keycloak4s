package com.fullfacing.keycloak4s.admin.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class InstallationProvider(val value: String) extends StringEnumEntry
case object InstallationProviders extends StringEnum[InstallationProvider] {
  case object Json     extends InstallationProvider("keycloak-oidc-keycloak-json")
  case object JBossXml extends InstallationProvider("keycloak-oidc-jboss-subsystem")

  def values: immutable.IndexedSeq[InstallationProvider] = findValues
}
