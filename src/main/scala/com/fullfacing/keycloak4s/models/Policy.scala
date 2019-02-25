package com.fullfacing.keycloak4s.models

import models.enums.{DecisionStrategy, Logic}

case class Policy(
                   config: Option[Map[_, _]],
                   decisionStrategy: Option[DecisionStrategy],
                   description: Option[String],
                   id: Option[String],
                   logic: Option[Logic],
                   name: Option[String],
                   owner: Option[String],
                   policies: Option[List[String]],
                   resources: Option[List[String]],
                   scopes: Option[List[String]],
                   `type`: Option[String]
                 )
