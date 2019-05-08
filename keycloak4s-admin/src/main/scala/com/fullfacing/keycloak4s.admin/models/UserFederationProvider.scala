package com.fullfacing.keycloak4s.admin.models

final case class UserFederationProvider(changedSyncPeriod: Option[Int],
                                        config: Option[Map[String, String]],
                                        displayName: Option[String],
                                        fullSyncPeriod: Option[Int],
                                        id: Option[String],
                                        lastSync: Option[Int],
                                        priority: Option[Int],
                                        providerName: Option[String])
