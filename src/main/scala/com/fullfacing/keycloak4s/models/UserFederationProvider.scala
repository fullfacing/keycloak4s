package com.fullfacing.keycloak4s.models

case class UserFederationProvider(changedSyncPeriod: Option[Int],
                                  config: Option[Map[_, _]],
                                  displayName: Option[String],
                                  fullSyncPeriod: Option[Int],
                                  id: Option[String],
                                  lastSync: Option[Int],
                                  priority: Option[Int],
                                  providerName: Option[String])
