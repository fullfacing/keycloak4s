package com.fullfacing.keycloak4s.admin.models

import com.fullfacing.keycloak4s.admin.models.enums.{DecisionStrategy, Logic}

final case class Policy(config: Option[Map[String, AnyRef]],
                        decisionStrategy: Option[DecisionStrategy],
                        description: Option[String],
                        id: Option[String],
                        logic: Option[Logic],
                        name: Option[String],
                        owner: Option[String],
                        policies: Option[List[String]],
                        resources: Option[List[String]],
                        scopes: Option[List[String]],
                        `type`: Option[String])
