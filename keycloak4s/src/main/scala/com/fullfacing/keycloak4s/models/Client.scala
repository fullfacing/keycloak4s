package com.fullfacing.keycloak4s.models

import java.util.UUID

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable

final case class Client(id: UUID,
                        clientId: String,
                        access: Option[ClientAccess],
                        adminUrl: Option[String],
                        attributes: Map[String, String] = Map.empty[String, String],
                        authenticationFlowBindingOverrides: Map[String, AnyRef] = Map.empty[String, String], //Potential for stronger typing, requires example.
                        authorizationServicesEnabled: Option[Boolean],
                        authorizationSettings: Option[ResourceServer],
                        baseUrl: Option[String],
                        bearerOnly: Boolean,
                        clientAuthenticatorType: Option[String],
                        consentRequired: Boolean,
                        defaultClientScopes: List[String],
                        defaultRoles: List[String],
                        description: Option[String],
                        directAccessGrantsEnabled: Boolean,
                        enabled: Boolean,
                        frontchannelLogout: Boolean,
                        fullScopeAllowed: Boolean,
                        implicitFlowEnabled: Boolean,
                        name: Option[String],
                        nodeReRegistrationTimeout: Option[Int],
                        notBefore: Option[Int],
                        optionalClientScopes: List[String],
                        origin: Option[String],
                        protocol: Client.Protocol,
                        protocolMappers: List[ProtocolMapper],
                        publicClient: Boolean,
                        redirectUris: List[String],
                        registeredNodes: List[Map[String, AnyRef]], //Potential for stronger typing, requires example.
                        registrationAccessToken: Option[String],
                        rootUrl: Option[String],
                        secret: Option[String],
                        serviceAccountsEnabled: Boolean,
                        standardFlowEnabled: Boolean,
                        surrogateAuthRequired: Boolean,
                        webOrigins: List[String])

object Client {
  final case class Create(clientId: String,
                          access: Option[ClientAccess] = None,
                          adminUrl: Option[String] = None,
                          attributes: Map[String, String] = Map.empty[String, String],
                          authenticationFlowBindingOverrides: Option[Map[String, AnyRef]] = None,
                          authorizationServicesEnabled: Option[Boolean] = None,
                          authorizationSettings: Option[ResourceServer] = None,
                          baseUrl: Option[String] = None,
                          bearerOnly: Option[Boolean] = None,
                          clientAuthenticatorType: Option[String] = None,
                          consentRequired: Boolean = false,
                          defaultClientScopes: Option[List[String]] = None,
                          defaultRoles: Option[List[String]] = None,
                          description: Option[String] = None,
                          directAccessGrantsEnabled: Option[Boolean] = None,
                          enabled: Boolean = true,
                          frontchannelLogout: Option[Boolean] = None,
                          fullScopeAllowed: Boolean = true,
                          implicitFlowEnabled: Option[Boolean] = None,
                          name: Option[String] = None,
                          nodeReRegistrationTimeout: Option[Int] = None,
                          notBefore: Option[Int] = None,
                          optionalClientScopes: Option[List[String]] = None,
                          origin: Option[String] = None,
                          protocol: Client.Protocol = Protocols.OpenIdConnect,
                          protocolMappers: Option[List[ProtocolMapper]] = None,
                          publicClient: Option[Boolean] = None,
                          redirectUris: List[String] = List.empty[String],
                          registeredNodes: List[Map[String, AnyRef]] = List.empty,
                          rootUrl: Option[String] = None,
                          serviceAccountsEnabled: Option[Boolean] = None,
                          standardFlowEnabled: Option[Boolean] = None,
                          surrogateAuthRequired: Option[Boolean] = None,
                          webOrigins: List[String] = List.empty[String])

  final case class Update(access: Option[ClientAccess] = None,
                          adminUrl: Option[String] = None,
                          attributes: Option[Map[String, String]] = None,
                          authenticationFlowBindingOverrides: Option[Map[String, AnyRef]] = None, //Potential for stronger typing, requires example.
                          authorizationServicesEnabled: Option[Boolean] = None,
                          authorizationSettings: Option[ResourceServer] = None,
                          baseUrl: Option[String] = None,
                          bearerOnly: Option[Boolean] = None,
                          clientAuthenticatorType: Option[String] = None,
                          clientId: Option[String] = None,
                          consentRequired: Option[Boolean] = None,
                          defaultClientScopes: Option[List[String]] = None,
                          defaultRoles: Option[List[String]] = None,
                          description: Option[String] = None,
                          directAccessGrantsEnabled: Option[Boolean] = None,
                          enabled: Option[Boolean] = None,
                          frontchannelLogout: Option[Boolean] = None,
                          fullScopeAllowed: Option[Boolean] = None,
                          implicitFlowEnabled: Option[Boolean] = None,
                          name: Option[String] = None,
                          nodeReRegistrationTimeout: Option[Int] = None,
                          notBefore: Option[Int] = None,
                          optionalClientScopes: Option[List[String]] = None,
                          origin: Option[String] = None,
                          protocol: Option[Protocol] = None,
                          protocolMappers: Option[List[ProtocolMapper]] = None,
                          publicClient: Option[Boolean] = None,
                          redirectUris: List[String] = List.empty[String],
                          registeredNodes: List[Map[String, AnyRef]] = List.empty, //Potential for stronger typing, requires example.
                          registrationAccessToken: Option[String] = None,
                          rootUrl: Option[String] = None,
                          secret: Option[String] = None,
                          serviceAccountsEnabled: Option[Boolean] = None,
                          standardFlowEnabled: Option[Boolean] = None,
                          surrogateAuthRequired: Option[Boolean] = None,
                          webOrigins: Option[List[String]] = None)

  sealed abstract class Protocol(val value: String) extends StringEnumEntry
  case object Protocols extends StringEnum[Protocol] {
    case object Saml          extends Protocol("saml")
    case object OpenIdConnect extends Protocol("openid-connect")

    val values: immutable.IndexedSeq[Protocol] = findValues
  }
}

final case class ClientAccess(view: Boolean,
                              configure: Boolean,
                              manage: Boolean)
