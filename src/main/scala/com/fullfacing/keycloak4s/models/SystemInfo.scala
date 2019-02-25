package models

case class SystemInfo(
                       fileEncoding: Option[String],
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
                       version: Option[String]
                     )
