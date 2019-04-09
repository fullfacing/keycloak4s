package com.fullfacing.keycloak4s.models

final case class ResourceServer(allowRemoteResourceManagement: Option[Boolean],
                                clientId: Option[String],
                                id: Option[String],
                                name: Option[String],
                                policies: Option[List[Policy]],
                                policyEnforcementMode: Option[List[String]],
                                resources: Option[List[Resource]],
                                scopes: Option[List[Scope]])