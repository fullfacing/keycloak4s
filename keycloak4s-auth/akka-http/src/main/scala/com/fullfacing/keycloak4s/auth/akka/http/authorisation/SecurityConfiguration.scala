package com.fullfacing.keycloak4s.auth.akka.http.authorisation

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.Uri.Path

trait SecurityConfiguration[A] {

  /**
   * Compares the request path to the server's security policy to determine which permissions are required
   * by the user and accepts or denies the request accordingly.
   *
   * @param path          The path of the HTTP request.
   * @param method        The HTTP method of the request.
   * @param configuration The security configuration of the server.
   * @param userRoles     The permissions of the user.
   */
  def authoriseRequest(path: Path, method: HttpMethod, configuration: A, userRoles: List[String]): Boolean
}
