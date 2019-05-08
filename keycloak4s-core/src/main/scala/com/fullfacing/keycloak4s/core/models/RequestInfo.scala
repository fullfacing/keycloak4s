package com.fullfacing.keycloak4s.core.models

/**
 * Contains information of a HTTP request.
 *
 * @param path      The URI path the request is sent to.
 * @param protocol  The HTTP method used.
 * @param body      The body of the request, if any.
 */
final case class RequestInfo(path: String,
                             protocol: String,
                             body: Option[Any] = None)
