package com.fullfacing.keycloak4s.models

case class UserFederationMapper(config: Option[Map[_, _]],
                                federationMapperType: Option[String],
                                federationProviderDisplayName: Option[String],
                                id: Option[String],
                                name: Option[String])
