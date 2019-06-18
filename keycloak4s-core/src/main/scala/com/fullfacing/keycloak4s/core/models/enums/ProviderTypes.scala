package com.fullfacing.keycloak4s.core.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

sealed abstract class ProviderType(val value: String) extends StringEnumEntry

case object ProviderTypes extends StringEnum[ProviderType] {
  case object BitBucket     extends ProviderType("bitbucket")
  case object Facebook      extends ProviderType("facebook")
  case object GitHub        extends ProviderType("github")
  case object GitLab        extends ProviderType("gitlab")
  case object Google        extends ProviderType("google")
  case object Instagram     extends ProviderType("instagram")
  case object KeycloakOidc  extends ProviderType("keycloak-oidc")
  case object LinkedIn      extends ProviderType("linkedin")
  case object Microsoft     extends ProviderType("microsoft")
  case object Oidc          extends ProviderType("oidc")
  case object OpenshiftV3   extends ProviderType("openshift-v3")
  case object PayPal        extends ProviderType("paypal")
  case object Saml          extends ProviderType("saml")
  case object StackOverflow extends ProviderType("stackoverflow")
  case object Twitter       extends ProviderType("twitter")

  val values: immutable.IndexedSeq[ProviderType] = findValues
}
