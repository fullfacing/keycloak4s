package com.fullfacing.keycloak4s.core.models

final case class MemoryInfo(free: Option[Long],
                            freeFormated: Option[String],
                            freePercentage: Option[Long],
                            total: Option[Long],
                            totalFormated: Option[String],
                            used: Option[Long],
                            usedFormated: Option[Long])