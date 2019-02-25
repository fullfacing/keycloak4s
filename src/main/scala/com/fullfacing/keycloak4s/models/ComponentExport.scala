package com.fullfacing.keycloak4s.models

case class ComponentExport(
                            config: Option[MultivaluedHashMap],
                            id: Option[String],
                            name: Option[String],
                            providerId: Option[String],
                            subComponents: Option[MultivaluedHashMap],
                            subType: Option[String]
                          )
