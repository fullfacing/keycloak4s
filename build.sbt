name         := "project-keycloak4s"
organization := "com.fullfacing"

lazy val global = {
  Seq(
    version       := "0.8.0-SNAPSHOT",
    scalaVersion  := "2.12.8",
    organization  := "com.fullfacing",
    scalacOptions ++= scalacOpts
  )
}

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


addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.9")
addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4")

val logback: Seq[ModuleID] = Seq(
  "ch.qos.logback" % "logback-core"    % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

val json4s: Seq[ModuleID] = Seq(
  "org.json4s" %% "json4s-jackson" % "3.6.5"
)

val enumeratum: Seq[ModuleID] = Seq(
  "com.beachape" %% "enumeratum-json4s" % "1.5.14"
)

val scalatest: Seq[ModuleID] = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalamock" %% "scalamock" % "4.1.0" % Test
)

val `sttp-akka`: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp" %% "core"              % "1.5.11",
  "com.softwaremill.sttp" %% "akka-http-backend" % "1.5.11",
  "com.typesafe.akka"     %% "akka-stream"       % "2.5.21",
  "com.softwaremill.sttp" %% "json4s"            % "1.5.11"
)

val cats: Seq[ModuleID] = Seq(
  "org.typelevel" %% "cats-core"   % "1.6.0",
  "org.typelevel" %% "cats-effect" % "1.2.0"
)

val monix: Seq[ModuleID] = Seq(
  "io.monix" %% "monix" % "3.0.0-RC2"
)

val `sttp-monix`: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp" %% "core"                            % "1.5.11",
  "com.softwaremill.sttp" %% "async-http-client-backend-monix" % "1.5.11",
  "com.softwaremill.sttp" %% "json4s"                          % "1.5.11"
)

val `akka-http`: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.8"
)

val nimbus: Seq[ModuleID] = Seq(
  "com.nimbusds" % "nimbus-jose-jwt" % "7.0.1"
)

// ----------------------------------------------- //
// Project and configuration for keycloak-monix    //
// ----------------------------------------------- //
lazy val `keycloak-dependencies`: Seq[ModuleID] = `sttp-akka` ++ cats ++ json4s ++ logback ++ monix ++ enumeratum

lazy val keycloak4s = (project in file("./keycloak4s"))
  .settings(global: _*)
  .settings(libraryDependencies ++= `keycloak-dependencies`)
  .settings(name := "keycloak4s", publishArtifact := true)


// ----------------------------------------------- //
// Project and configuration for keycloak-monix    //
// ----------------------------------------------- //
lazy val `keycloak-monix-dependencies`: Seq[ModuleID] = `sttp-monix` ++ monix

lazy val `keycloak4s-monix` = (project in file("./keycloak4s-monix"))
  .settings(global: _*)
  .settings(libraryDependencies ++= `keycloak-monix-dependencies`)
  .settings(name := "keycloak4s-monix", publishArtifact := true)
  .dependsOn(keycloak4s)

// -------------------------------------------------------- //
// Project and configuration for keycloak-akka-http-adapter //
// -------------------------------------------------------- //
lazy val `keycloak-akka-http-dependencies`: Seq[ModuleID] = `akka-http` ++ nimbus ++ scalatest

lazy val `keycloak4s-akka-http` = (project in file("./keycloak4s-adapters/akka-http"))
  .settings(global: _*)
  .settings(libraryDependencies ++= `keycloak-akka-http-dependencies`)
  .settings(name := "keycloak4s-akka-http-adapter", publishArtifact := true)
  .dependsOn(keycloak4s)

// ---------------------------------------------- //
// Project and configuration for the root project //
// ---------------------------------------------- //
lazy val root = (project in file("."))
  .settings(global: _*)
  .settings(publishArtifact := false)
  .aggregate(keycloak4s, `keycloak4s-monix`, `keycloak4s-akka-http`)