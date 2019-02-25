package com.fullfacing.keycloak4s.models

import models.enums.PolicyEnforcementMode

case class ResourceServer(
                           allowRemoteResourceManagement: Option[Boolean],
                           clientId: Option[String],
                           id: Option[String],
                           name: Option[String],
                           policies: Option[List[Policy]],
                           policyEnforcementMode: Option[List[PolicyEnforcementMode]],
                           resources: Option[List[Resource]],
                           scopes: Option[List[Scope]]

                         )