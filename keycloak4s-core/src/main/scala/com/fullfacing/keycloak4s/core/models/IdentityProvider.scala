package com.fullfacing.keycloak4s.core.models

final case class IdentityProvider(addReadTokenRoleOnCreate: Option[Boolean],
                                  alias: Option[String],
                                  config: Option[Map[String, String]],
                                  displayName: Option[String],
                                  enabled: Option[Boolean],
                                  firstBrokerLoginFlowAlias: Option[String],
                                  internalId: Option[String],
                                  linkOnly: Option[Boolean],
                                  postBrokerLoginFlowAlias: Option[String],
                                  providerId: Option[String],
                                  storeToken: Option[Boolean],
                                  trustEmail: Option[Boolean])
