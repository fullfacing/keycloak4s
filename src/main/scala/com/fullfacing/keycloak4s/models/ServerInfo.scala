package com.fullfacing.keycloak4s.models

case class ServerInfo(builtinProtocolMappers: Option[Map[Any, Any]],
                      clientImporters: Option[List[Map[Any, Any]]],
                      clientInstallations: Option[Map[Any, Any]],
                      componentTypes: Option[Map[Any, Any]],
                      enums: Option[Map[Any, Any]],
                      identityProviders: Option[List[Map[Any, Any]]],
                      memoryInfo: Option[MemoryInfo],
                      passwordPolicies: Option[PasswordPolicyType],
                      profileInfo: Option[ProfileInfo],
                      protocolMapperTypes: Option[Map[Any, Any]],
                      providers: Option[Map[Any, Any]],
                      socialProviders: Option[List[Map[Any, Any]]],
                      systemInfo: Option[SystemInfo],
                      themes: Option[Map[Any, Any]])
