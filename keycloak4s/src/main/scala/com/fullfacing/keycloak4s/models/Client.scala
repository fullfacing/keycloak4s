package com.fullfacing.keycloak4s.models

final case class Client(id: String,
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
                        fullScopeAllowed: Option[Boolean],
                        implicitFlowEnabled: Option[Boolean],
                        name: Option[String],
                        nodeReRegistrationTimeout: Option[Int],
                        notBefore: Option[Int],
                        optionalClientScopes: List[String],
                        origin: Option[String],
                        protocol: String,
                        protocolMappers: List[ProtocolMapper],
                        publicClient: Option[Boolean],
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
                          enabled: Boolean = true,
                          attributes: Map[String, String] = Map.empty[String, String])
}

final case class ClientAccess(view: Boolean,
                              configure: Boolean,
                              manage: Boolean)
