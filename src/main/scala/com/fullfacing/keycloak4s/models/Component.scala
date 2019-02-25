package com.fullfacing.keycloak4s.models

case class Component(
                      config: Option[MultivaluedHashMap],
                      id: Option[String],
                      name: Option[String],
                      parentId: Option[String],
                      providerId: Option[String],
                      providerType: Option[String],
                      subType: Option[String]
                    )