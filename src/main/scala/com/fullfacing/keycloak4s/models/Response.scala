package com.fullfacing.keycloak4s.models

case class Response(status: Option[Int],
                    reason: Option[String],
                    hasEntity: Option[Boolean],
                    closed: Option[Boolean],
                    buffered: Option[Boolean])
