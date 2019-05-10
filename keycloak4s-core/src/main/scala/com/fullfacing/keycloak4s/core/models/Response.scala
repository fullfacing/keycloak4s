package com.fullfacing.keycloak4s.core.models

final case class Response(status: Option[Int] = None,
                          reason: Option[String] = None,
                          hasEntity: Option[Boolean] = None,
                          closed: Option[Boolean] = None,
                          buffered: Option[Boolean] = None)
