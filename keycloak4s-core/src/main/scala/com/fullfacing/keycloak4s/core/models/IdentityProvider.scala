package com.fullfacing.keycloak4s.core.models

final case class IdentityProvider(addReadTokenRoleOnCreate: Option[Boolean] = None,
                                  alias: Option[String] = None,
                                  config: Option[Map[String, String]] = None,
                                  displayName: Option[String] = None,
                                  enabled: Option[Boolean] = None,
                                  firstBrokerLoginFlowAlias: Option[String] = None,
                                  internalId: Option[String] = None,
                                  linkOnly: Option[Boolean] = None,
                                  postBrokerLoginFlowAlias: Option[String] = None,
                                  providerId: Option[String] = None,
                                  storeToken: Option[Boolean] = None,
                                  trustEmail: Option[Boolean] = None)
