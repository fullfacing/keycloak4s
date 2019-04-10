package com.fullfacing.keycloak4s.models

import java.util.UUID

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

final case class User(username: String,
                      access: Option[UserAccess] = None,
                      attributes: Map[String, List[String]] = Map.empty[String, List[String]],
                      clientConsents: List[UserConsent] = List.empty[UserConsent],
                      clientRoles: Map[String, Any] = Map.empty[String, String],
                      credentials: List[Credential] = List.empty[Credential],
                      disableableCredentialTypes: List[String] = List.empty[String],
                      email: Option[String] = None,
                      emailVerified: Option[Boolean] = None,
                      enabled: Option[Boolean] = None,
                      federatedIdentities: List[FederatedIdentity] = List.empty[FederatedIdentity],
                      federationLink: Option[String] = None,
                      firstName: Option[String] = None,
                      groups: List[String] = List.empty[String],
                      lastName: Option[String] = None,
                      notBefore: Option[String] = None,
                      origin: Option[String] = None,
                      realmRoles: List[String] = List.empty[String],
                      requiredActions: List[User.RequiredAction] = List.empty[User.RequiredAction],
                      self: Option[String] = None,
                      serviceAccountClientId: Option[String] = None,
                      createdTimestamp: Long,
                      id: UUID)

object User {

  sealed abstract class RequiredAction(val value: String) extends StringEnumEntry
  case object RequiredActions extends StringEnum[RequiredAction] {
    case object VerifyEmail        extends RequiredAction("VERIFY_EMAIL")
    case object ConfigureTotp      extends RequiredAction("CONFIGURE_TOTP")
    case object UpdateProfile      extends RequiredAction("UPDATE_PROFILE")
    case object UpdatePassword     extends RequiredAction("UPDATE_PASSWORD")
    case object TermsAndConditions extends RequiredAction("terms_and_conditions")

    val values: immutable.IndexedSeq[RequiredAction] = findValues
  }

  final case class Create(username: String,
                          enabled: Boolean,
                          credentials: List[Credential] = List.empty[Credential],
                          attributes: Map[String, List[String]] = Map.empty[String, List[String]],
                          email: Option[String] = None,
                          emailVerified: Option[Boolean] = None,
                          firstName: Option[String] = None,
                          lastName: Option[String] = None,
                          federationLink: Option[String] = None,
                          requiredActions: List[RequiredAction] = List.empty[RequiredAction])
}

