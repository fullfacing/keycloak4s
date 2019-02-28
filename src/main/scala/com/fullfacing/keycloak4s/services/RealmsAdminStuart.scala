package com.fullfacing.keycloak4s.services

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.apollo.core.protocol.NoContent
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object RealmsAdminStuart {


  /**
   * Delete all events
   *
   * @param realm realm name (not id!)
   * @return
   */
  def deleteAllEvents(realm: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "events")
    SttpClient.delete(path)
  }

  /**
   * Get the events provider configuration
   * Returns JSON object with events provider configuration
   *
   * @param realm realm name (not id!)
   * @return
   */
  def getEventsConfig(realm: String): AsyncApolloResponse[RealmEventsConfig] = {
    val path = Seq(realm, "events", "config")
    SttpClient.get(path)
  }

  /**
   * Update the events provider.
   * Change the events provider and/or its configuration
   *
   * @param realm realm name (not id!)
   * @param config
   * @return
   */
  def updateEventsConfig(realm: String, config: RealmEventsConfig): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "events", "config")
    SttpClient.put(config, path)
  }

  /**
   * GET /{realm}/group-by-path/{path}
   *
   * @param realm realm name (not id!)
   * @param path
   * @return
   */
  def getGroupByPath(realm: String, path: String): AsyncApolloResponse[Group] = {
    SttpClient.get(Seq(realm, "group-by-path", path))
  }

  /**
   * Removes all user sessions.
   * Any client that has an admin url will also be told to invalidate any sessions they have.
   *
   * @param realm realm name (not id!)
   * @return
   */
  def logoutAll(realm: String): AsyncApolloResponse[GlobalRequestResult] = {
    val path = Seq(realm, "logout-all")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Partial export of existing realm into a JSON file.
   *
   * @param realm realm name (not id!)
   * @param exportClients
   * @param exportGroupsAndRoles
   * @return
   */
  def partialExport(realm: String,
                    exportClients: Option[Boolean] = None,
                    exportGroupsAndRoles: Option[Boolean] = None): AsyncApolloResponse[RealmRepresentation] = {
    val path    = Seq(realm, "partial-export")
    val queries = createQuery(("exportClients", exportClients), ("exportGroupsAndRoles", exportGroupsAndRoles))

    SttpClient.post(path, queries)
  }

  /**
   * Partial import from a JSON file to an existing realm.
   *
   * @param realm realm name (not id!)
   * @param rep
   * @return
   */
  def partialImport(realm: String, rep: PartialImport): AsyncApolloResponse[TODO] = {
    val path = Seq(realm, "partialImport")
    SttpClient.post(rep, path)
  }

  /**
   * Push the realm’s revocation policy to any client that has an admin url associated with it.
   *
   * @param realm realm name (not id!)
   * @return
   */
  def pushRevocation(realm: String): AsyncApolloResponse[GlobalRequestResult] = {
    val path = Seq(realm, "push-revocation")
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * Remove a specific user session.
   * Any client that has an admin url will also be told to invalidate this particular session.
   *
   * @param realm   realm name (not id!)
   * @param session
   * @return
   */
  def removeUserSession(realm: String, session: String): AsyncApolloResponse[NoContent] = {
    val path = Seq(realm, "sessions", session)
    SttpClient.delete(path)
  }

  def testLdapConnection(realm: String, // TODO Figure out how queries should be sent
                         action: Option[String] = None,
                         bindCredential: Option[String] = None,
                         bindDn: Option[String] = None,
                         componentId: Option[String] = None,
                         connectionTimeout: Option[String] = None,
                         connectionUrl: Option[String] = None,
                         useTruststoreSpi: Option[String] = None): AsyncApolloResponse[TODO] = {

    val path = Seq(realm, "testLDAPConnection")
    val queries = createQuery(
      ("action", action),
      ("bindCredential", bindCredential),
      ("bindDn", bindDn),
      ("componentId", componentId),
      ("connectionTimeout", connectionTimeout),
      ("connectionUrl", connectionUrl),
      ("useTruststoreSpi", useTruststoreSpi)
    )

    SttpClient.post(path, queries)
  }

  /**
   * Test SMTP connection with current logged in user
   *
   * @param realm  realm name (not id!)
   * @param config SMTP server configuration
   * @return
   */
  def testSmtpConnection(realm: String, config: String): AsyncApolloResponse[TODO] = {
    val path = Seq(realm, "testSMTPConnection", config)
    SttpClient.post(path, Seq.empty[KeyValue])
  }

  /**
   * GET /{realm}/users-management-permissions
   *
   * @param realm realm name (not id!)
   * @return
   */
  def getUsersManagementPermissions(realm: String): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, "users-management-permissions")
    SttpClient.get(path)
  }

  /**
   * PUT /{realm}/users-management-permissions
   *
   * @param realm realm name (not id!)
   * @param ref
   * @return
   */
  def updateUsersManagementPermissions(realm: String, ref: ManagementPermission): AsyncApolloResponse[ManagementPermission] = {
    val path = Seq(realm, "users-management-permissions")
    SttpClient.put(ref, path)
  }
}
