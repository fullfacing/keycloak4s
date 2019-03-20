package com.fullfacing.keycloak4s.models

case class Policy(config: Option[Map[String, AnyRef]],
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
