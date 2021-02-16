package com.fullfacing.keycloak4s.authz.monix.bio.models

import java.util.UUID

final case class PermissionTicket(id: UUID,
                                  owner: UUID,
                                  resource: UUID,
                                  scope: UUID,
                                  requester: UUID,
                                  granted: Boolean,
                                  scopeName: Option[String],
                                  resourceName: Option[String],
                                  ownerName: Option[String],
                                  requesterName: Option[String])

object PermissionTicket {

  final case class Create(resource: UUID,
                          scope: Option[UUID] = None,
                          requester: Option[UUID] = None,
                          granted: Boolean,
                          scopeName: Option[String] = None,
                          requesterName: Option[String] = None)

  final case class Request(resource_id: String,
                           resource_scopes: List[String] = List.empty[String],
                           resource_server_id: Option[String] = None,
                           claims: Map[String, List[String]] = Map.empty[String, List[String]])

  final case class Response(ticket: String)
}
