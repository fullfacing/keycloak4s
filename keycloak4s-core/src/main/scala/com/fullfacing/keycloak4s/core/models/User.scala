package com.fullfacing.keycloak4s.core.models

import java.util.UUID

final case class User(username: String,
                      access: Option[UserAccess] = None,
                      attributes: Map[String, List[String]] = Map.empty[String, List[String]],
                      clientConsents: List[UserConsent] = List.empty[UserConsent],
                      clientRoles: Map[String, Any] = Map.empty[String, String],
                      credentials: List[Credential] = List.empty[Credential],
                      disableableCredentialTypes: List[String] = List.empty[String],
                      email: Option[String] = None,
                      emailVerified: Boolean,
                      enabled: Boolean,
                      totp: Boolean,
                      federatedIdentities: List[FederatedIdentity] = List.empty[FederatedIdentity],
                      federationLink: Option[String] = None,
                      firstName: Option[String] = None,
                      groups: List[String] = List.empty[String],
                      lastName: Option[String] = None,
                      notBefore: Option[String] = None,
                      origin: Option[String] = None,
                      realmRoles: List[String] = List.empty[String],
                      requiredActions: List[RequiredAction] = List.empty[RequiredAction],
                      self: Option[String] = None,
                      serviceAccountClientId: Option[String] = None,
                      createdTimestamp: Long,
                      id: UUID)

object User {

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

  final case class Update(username: Option[String] = None,
                          access: Option[UserAccess] = None,
                          attributes: Map[String, List[String]] = Map.empty[String, List[String]],
                          clientConsents: List[UserConsent] = List.empty[UserConsent],
                          clientRoles: Map[String, Any] = Map.empty[String, String],
                          credentials: List[Credential] = List.empty[Credential],
                          disableableCredentialTypes: List[String] = List.empty[String],
                          email: Option[String] = None,
                          emailVerified: Option[Boolean] = None,
                          enabled: Option[Boolean] = None,
                          totp: Option[Boolean] = None,
                          federatedIdentities: List[FederatedIdentity] = List.empty[FederatedIdentity],
                          federationLink: Option[String] = None,
                          firstName: Option[String] = None,
                          groups: List[String] = List.empty[String],
                          lastName: Option[String] = None,
                          notBefore: Option[String] = None,
                          origin: Option[String] = None,
                          realmRoles: List[String] = List.empty[String],
                          requiredActions: List[RequiredAction] = List.empty[RequiredAction],
                          self: Option[String] = None,
                          serviceAccountClientId: Option[String] = None,
                          createdTimestamp: Option[Long] = None,
                          id: Option[UUID] = None)
}

