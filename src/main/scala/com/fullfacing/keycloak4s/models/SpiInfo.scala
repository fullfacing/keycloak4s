package com.fullfacing.keycloak4s.models

case class SpiInfo(
                    internal: Option[Boolean],
                    providers: Option[Map[_, _]]
                  )
