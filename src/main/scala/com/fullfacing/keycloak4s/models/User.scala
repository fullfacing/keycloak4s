package models

case class User(
                 access: Option[Map[_, _]],
                 attributes: Option[Map[_, _]],
                 clientConsents: Option[List[UserConsent]],
                 clientRoles: Option[Map[_, _]],
                 createdTimestamp: Option[Long],
                 credentials: Option[List[Credential]],
                 disableableCredentialTypes: Option[List[String]],
                 email: Option[String],
                 emailVerified: Option[Boolean],
                 enabled: Option[Boolean],
                 federatedIdentities: Option[List[FederatedIdentity]],
                 federationLink: Option[String],
                 firstName: Option[String],
                 groups: Option[List[String]],
                 id: Option[String],
                 lastName: Option[String],
                 notBefore: Option[String],
                 origin: Option[String],
                 realmRoles: Option[List[String]],
                 requiredActions: Option[List[String]],
                 self: Option[String],
                 serviceAccountClientId: Option[String],
                 username: Option[String]
               )
