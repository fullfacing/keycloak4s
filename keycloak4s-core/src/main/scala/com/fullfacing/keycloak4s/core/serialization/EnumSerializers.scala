package com.fullfacing.keycloak4s.core.serialization

import com.fullfacing.keycloak4s.core.models.enums._
import enumeratum.values.Json4s

object EnumSerializers {
  val all = List(
    Json4s.serializer(Categories),
    Json4s.serializer(DecisionStrategies),
    Json4s.serializer(EventTypes),
    Json4s.serializer(HttpMethods),
    Json4s.serializer(LogicTypes),
    Json4s.serializer(PolicyEnforcementModes),
    Json4s.serializer(PolicyTypes),
    Json4s.serializer(CredentialTypes),
    Json4s.serializer(Directions),
    Json4s.serializer(InstallationProviders),
    Json4s.serializer(Protocols),
    Json4s.serializer(RequiredActions),
    Json4s.serializer(TriggerSyncActions)
  )
}
