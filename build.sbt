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

val keycloak = {
  val version = "4.8.2.Final"
  Seq(
    "org.keycloak" % "keycloak-authz-client" % version,
    "org.keycloak" % "keycloak-admin-client" % version
  )
}

val enumeratum_Json4s = {
  val version ="1.5.13"
  Seq("com.beachape" %% "enumeratum-json4s" % version)
}

val sttp = {
  val version = "1.5.11"
  Seq(
    "com.softwaremill.sttp" %% "json4s" % version,
    "com.softwaremill.sttp" %% "async-http-client-backend-monix" % version
  )
}

libraryDependencies := keycloak ++ enumeratum_Json4s ++ sttp