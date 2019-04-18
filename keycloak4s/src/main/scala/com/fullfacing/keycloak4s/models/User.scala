package com.fullfacing.keycloak4s.models

import java.util.UUID

import com.fullfacing.keycloak4s.models.enums.RequiredAction

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
}

