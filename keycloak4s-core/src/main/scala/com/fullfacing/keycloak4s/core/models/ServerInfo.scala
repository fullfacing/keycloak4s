package com.fullfacing.keycloak4s.core.models

final case class ServerInfo(builtinProtocolMappers: Option[Map[String, Any]],
                            clientImporters: Option[List[Map[String, Any]]],
                            clientInstallations: Option[Map[String, Any]],
                            componentTypes: Option[Map[String, Any]],
                            enums: Option[Map[String, Any]],
                            identityProviders: Option[List[Map[String, Any]]],
                            memoryInfo: Option[ServerInfo.MemoryInfo],
                            passwordPolicies: Option[ServerInfo.PasswordPolicyType],
                            profileInfo: Option[ServerInfo.ProfileInfo],
                            protocolMapperTypes: Option[Map[String, Any]],
                            providers: Option[Map[String, Any]],
                            socialProviders: Option[List[Map[String, Any]]],
                            systemInfo: Option[ServerInfo.SystemInfo],
                            themes: Option[Map[String, Any]])

object ServerInfo {
  final case class MemoryInfo(free: Option[Long],
                              freeFormated: Option[String],
                              freePercentage: Option[Long],
                              total: Option[Long],
                              totalFormated: Option[String],
                              used: Option[Long],
                              usedFormated: Option[Long])

  final case class PasswordPolicyType(configType: Option[String],
                                      defaultValue: Option[String],
                                      displayName: Option[String],
                                      id: Option[String],
                                      multipleSupported: Option[Boolean])

  final case class ProfileInfo(disabledFeatures: Option[List[String]],
                               experimentalFeatures: Option[List[String]],
                               name: Option[String],
                               previewFeatures: Option[List[String]])

  final case class SystemInfo(fileEncoding: Option[String],
                              javaHome: Option[String],
                              javaRuntime: Option[String],
                              javaVendor: Option[String],
                              javaVersion: Option[String],
                              javaVm: Option[String],
                              javaVmVersion: Option[String],
                              osArchitecture: Option[String],
                              osName: Option[String],
                              osVersion: Option[String],
                              serverTime: Option[String],
                              uptime: Option[String],
                              uptimeMillis: Option[String],
                              userDir: Option[String],
                              userLocale: Option[String],
                              userName: Option[String],
                              userTimezone: Option[String],
                              version: Option[String])
}
