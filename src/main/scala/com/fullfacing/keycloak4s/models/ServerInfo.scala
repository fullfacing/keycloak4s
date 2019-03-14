package com.fullfacing.keycloak4s.models

case class ServerInfo(builtinProtocolMappers: Option[Map[String, Any]],
                      clientImporters: Option[List[Map[String, Any]]],
                      clientInstallations: Option[Map[String, Any]],
                      componentTypes: Option[Map[String, Any]],
                      enums: Option[Map[String, Any]],
                      identityProviders: Option[List[Map[String, Any]]],
                      memoryInfo: Option[MemoryInfo],
                      passwordPolicies: Option[PasswordPolicyType],
                      profileInfo: Option[ProfileInfo],
                      protocolMapperTypes: Option[Map[String, Any]],
                      providers: Option[Map[String, Any]],
                      socialProviders: Option[List[Map[String, Any]]],
                      systemInfo: Option[SystemInfo],
                      themes: Option[Map[String, Any]])
