package models

case class ServerInfo(
                       builtinProtocolMappers: Option[Map[_, _]],
                       clientImporters: Option[List[Map[_, _]]],
                       clientInstallations: Option[Map[_, _]],
                       componentTypes: Option[Map[_, _]],
                       enums: Option[Map[_, _]],
                       identityProviders: Option[List[Map[_, _]]],
                       memoryInfo: Option[MemoryInfo],
                       passwordPolicies: Option[PasswordPolicyType],
                       profileInfo: Option[ProfileInfo],
                       protocolMapperTypes: Option[Map[_, _]],
                       providers: Option[Map[_, _]],
                       socialProviders: Option[List[Map[_, _]]],
                       systemInfo: Option[SystemInfo],
                       themes: Option[Map[_, _]]
                     )
