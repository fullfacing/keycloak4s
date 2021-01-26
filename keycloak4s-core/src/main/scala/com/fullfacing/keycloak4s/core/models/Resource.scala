package com.fullfacing.keycloak4s.core.models

final case class Resource(id: Option[String],
                          attributes: Option[Map[String, List[String]]],
                          displayName: Option[String],
                          icon_uri: Option[String],
                          name: Option[String],
                          ownerManagedAccess: Option[Boolean],
                          scopes: Option[List[Scope]],
                          `type`: Option[String],
                          uris: Option[List[String]])

object Resource {

  final case class Create(attributes: Option[Map[String, List[String]]],
                          displayName: Option[String],
                          icon_uri: Option[String],
                          name: Option[String],
                          ownerManagedAccess: Option[Boolean],
                          scopes: Option[List[Scope]],
                          `type`: Option[String],
                          uris: Option[List[String]])

  final case class Update(attributes: Option[Map[String, List[String]]] = None,
                          displayName: Option[String] = None,
                          icon_uri: Option[String] = None,
                          name: Option[String] = None,
                          ownerManagedAccess: Option[Boolean] = None,
                          scopes: Option[List[Scope]] = None,
                          `type`: Option[String] = None,
                          uris: Option[List[String]] = None)
}
