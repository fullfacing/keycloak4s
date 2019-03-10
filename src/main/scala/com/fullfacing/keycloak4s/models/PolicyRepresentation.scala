package com.fullfacing.keycloak4s.models

case class PolicyRepresentation(config: Option[Map[_, _]],
                                decisionStrategy: Option[String],
                                description: Option[String],
                                id: Option[String],
                                logic: Option[String],
                                name: Option[String],
                                owner: Option[String],
                                policies: Option[List[String]],
                                resources: Option[List[String]],
                                scopes: Option[List[String]],
                                `type`: Option[String])
