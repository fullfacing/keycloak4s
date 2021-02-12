package com.fullfacing.keycloak4s.authz.monix.bio.models

import java.util.UUID

final case class PermissionTicket(id: UUID,
                                  owner: String,
                                  resource: String,
                                  scope: String,
                                  granted: Boolean,
                                  scopeName: String,
                                  resourceName: String,
                                  requester: String,
                                  ownerName: String,
                                  requesterName: String)

object PermissionTicket {

  final case class Create(owner: Option[String] = None,
                          ownerName: Option[String] = None,
                          resource: Option[String] = None,
                          resourceName: Option[String] = None,
                          scope: Option[String] = None,
                          scopeName: Option[String] = None,
                          granted: Option[Boolean] = None,
                          requester: Option[String] = None,
                          requesterName: Option[String] = None)

  final case class Request(resource_id: String,
                           resource_scopes: List[String] = List.empty[String],
                           resource_server_id: Option[String] = None,
                           claims: Map[String, List[String]] = Map.empty[String, List[String]])

  final case class Response(ticket: String)
}
