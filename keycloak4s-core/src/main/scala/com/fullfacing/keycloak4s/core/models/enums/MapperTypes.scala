package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class MapperType(val value: String) extends StringEnumEntry

case object MapperTypes extends StringEnum[MapperType] {
  case object AttributeImporterOidc           extends MapperType("oidc-user-attribute-idp-mapper")
  case object AttributeImporterFacebook       extends MapperType("facebook-user-attribute-mapper")
  case object AttributeImporterGithub         extends MapperType("github-user-attribute-mapper")
  case object AttributeImporterGoogle         extends MapperType("google-user-attribute-mapper")
  case object AttributeImporterInstagram      extends MapperType("instagram-user-attribute-mapper")
  case object AttributeImporterLinkedIn       extends MapperType("linkedinuser-attribute-mapper")
  case object AttributeImporterMicrosoft      extends MapperType("microsoft-user-attribute-mapper")
  case object AttributeImporterPayPal         extends MapperType("paypal-user-attribute-mapper")
  case object AttributeImporterSaml           extends MapperType("saml-user-attribute-idp-mapper")
  case object AttributeImporterStackOverflow  extends MapperType("stackoverflow-user-attribute-mapper")
  case object ClaimToRole                     extends MapperType("oidc-role-idp-mapper")
  case object ExternalRoleToRole              extends MapperType("keycloak-oidc-role-to-role-idp-mapper")
  case object HardcodedAttribute              extends MapperType("hardcoded-attribute-idp-mapper")
  case object HardcodedRole                   extends MapperType("oidc-hardcoded-role-idp-mapper")
  case object HardcodedUserSessionAttribute   extends MapperType("hardcoded-user-session-attribute-idp-mapper")
  case object SamlAttributeToRole             extends MapperType("saml-role-idp-mapper")
  case object UsernameTemplateImporterOidc    extends MapperType("oidc-username-idp-mapper")
  case object UsernameTemplateImporterSaml    extends MapperType("saml-username-idp-mapper")
  case object AdvancedRole                    extends MapperType("oidc-advanced-role-idp-mapper")

  val values: immutable.IndexedSeq[MapperType] = findValues
}
