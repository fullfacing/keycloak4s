package com.fullfacing.keycloak4s.core.models

import com.fullfacing.keycloak4s.core.models.Resource.Owner

import java.util.UUID

final case class Resource(_id: UUID,
                          name: String,
                          owner: Owner,
                          attributes: Option[Map[String, List[String]]],
                          displayName: Option[String],
                          icon_uri: Option[String],
                          ownerManagedAccess: Boolean,
                          resource_scopes: List[Scope],
                          `type`: Option[String],
                          uris: List[String])

object Resource {

  final case class Owner(id: UUID,
                         name: String)

  final case class Create(name: String,
                          attributes: Map[String, List[String]] = Map.empty[String, List[String]],
                          displayName: Option[String] = None,
                          icon_uri: Option[String] = None,
                          ownerManagedAccess: Option[Boolean] = None,
                          scopes: Option[List[Scope]] = None,
                          `type`: Option[String] = None,
                          uris: Option[List[String]] = None)
}
