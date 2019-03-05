package com.fullfacing.keycloak4s

import com.fullfacing.keycloak4s.models.enums._
import enumeratum.Json4s

object EnumSerializers {
  val all = List(
    Json4s.serializer(Category),
    Json4s.serializer(DecisionStrategy),
    Json4s.serializer(Logic),
    Json4s.serializer(Policy),
    Json4s.serializer(PolicyEnforcementMode)
  )
}