package com.fullfacing.keycloak4s.core.models

import com.fullfacing.keycloak4s.core.models.enums.ProviderType

final case class IdentityProvider(addReadTokenRoleOnCreate: Boolean,
                                  alias: String,
                                  config: Map[String, String],
                                  displayName: Option[String],
                                  enabled: Boolean,
                                  firstBrokerLoginFlowAlias: String,
                                  internalId: String,
                                  linkOnly: Boolean,
                                  postBrokerLoginFlowAlias: Option[String],
                                  providerId: ProviderType,
                                  storeToken: Boolean,
                                  trustEmail: Boolean)

object IdentityProvider {
  final case class Create(alias: String,
                          providerId: ProviderType,
                          config: Map[String, String],
                          addReadTokenRoleOnCreate: Option[Boolean] = None,
                          displayName: Option[String] = None,
                          enabled: Option[Boolean] = None,
                          firstBrokerLoginFlowAlias: Option[String] = None,
                          internalId: Option[String] = None,
                          linkOnly: Option[Boolean] = None,
                          postBrokerLoginFlowAlias: Option[String] = None,
                          storeToken: Option[Boolean] = None,
                          trustEmail: Option[Boolean] = None)

  final case class Update(alias: String,
                          providerId: Option[ProviderType] = None,
                          config: Option[Map[String, String]] = None,
                          addReadTokenRoleOnCreate: Option[Boolean] = None,
                          displayName: Option[String] = None,
                          enabled: Option[Boolean] = None,
                          firstBrokerLoginFlowAlias: Option[String] = None,
                          internalId: Option[String] = None,
                          linkOnly: Option[Boolean] = None,
                          postBrokerLoginFlowAlias: Option[String] = None,
                          storeToken: Option[Boolean] = None,
                          trustEmail: Option[Boolean] = None)

  final case class Type(name: String,
                        id: String)
}
