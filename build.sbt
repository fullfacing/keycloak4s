name         := "keycloak4s"
version      := "0.1.1"
organization := "com.fullfacing"

val scalacOpts = Seq(
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ypartial-unification",
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

scalaVersion := "2.12.8"
scalacOptions ++= scalacOpts
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

val logback: Seq[ModuleID] = {
  val version = "1.2.3"
  Seq(
    "ch.qos.logback" % "logback-core"    % version,
    "ch.qos.logback" % "logback-classic" % version
  )
}

val keycloak: Seq[ModuleID] = {
  val version = "4.8.2.Final"
  Seq(
    "org.keycloak" % "keycloak-authz-client" % version,
    "org.keycloak" % "keycloak-admin-client" % version
  )
}

val json4s: Seq[ModuleID] = {
  val version = "3.6.5"
  Seq(
    "org.json4s" %% "json4s-native" % version
  )
}

val enumeratum: Seq[ModuleID] = Seq(
  "com.beachape" %% "enumeratum-json4s" % "1.5.14"
)

val sttp: Seq[ModuleID] = {
  val version = "1.5.11"
  Seq(
    "com.softwaremill.sttp" %% "core"   % version,
    "com.softwaremill.sttp" %% "json4s" % version
  )
}

val cats: Seq[ModuleID] = {
  val version = "1.6.0"
  Seq("org.typelevel" %% "cats-core" % version)
}

libraryDependencies := keycloak ++ cats ++ json4s ++ sttp ++ enumeratum ++ logback