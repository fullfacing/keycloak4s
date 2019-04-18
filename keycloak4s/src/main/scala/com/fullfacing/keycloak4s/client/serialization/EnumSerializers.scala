package com.fullfacing.keycloak4s.client.serialization

import com.fullfacing.keycloak4s.models.enums._
import enumeratum.Json4s
import enumeratum.values.{Json4s => Json4sValues}

object EnumSerializers {
  val all = List(
    Json4s.serializer(Categories),
    Json4s.serializer(DecisionStrategies),
    Json4s.serializer(EventTypes),
    Json4s.serializer(LogicTypes),
    Json4s.serializer(PolicyEnforcementModes),
    Json4s.serializer(PolicyTypes),
    Json4sValues.serializer(CredentialTypes),
    Json4sValues.serializer(Directions),
    Json4sValues.serializer(InstallationProviders),
    Json4sValues.serializer(Protocols),
    Json4sValues.serializer(RequiredActions),
    Json4sValues.serializer(TriggerSyncActions)
  )
}
