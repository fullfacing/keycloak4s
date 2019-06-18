name         := "project-keycloak4s"
organization := "com.fullfacing"

lazy val global = {
  Seq(
    version       := "0.16.0-SNAPSHOT",
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
addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.0")

coverageExcludedPackages := "<empty>;com.fullfacing.transport.*"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// ---------------------------------- //
//          Library Versions          //
// ---------------------------------- //
val akkaHttpVersion     = "10.1.8"
val akkaStreamsVersion  = "2.5.23"
val catsCoreVersion     = "1.6.0"
val catsEffectVersion   = "1.3.0"
val enumeratumVersion   = "1.5.14"
val json4sVersion       = "3.6.5"
val logbackVersion      = "1.2.3"
val monixVersion        = "3.0.0-RC2"
val nimbusVersion       = "7.2.1"
val scalaMeterVersion   = "0.17"
val scalaTestVersion    = "3.0.5"
val sttpVersion         = "1.5.17"

// -------------------------------------- //
//          Library Dependencies          //
// -------------------------------------- //
val akkaHttp: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-stream"  % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http"    % akkaHttpVersion
)

val cats: Seq[ModuleID] = Seq(
  "org.typelevel" %% "cats-core"   % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion
)

val enumeratum: Seq[ModuleID] = Seq(
  "com.beachape" %% "enumeratum-json4s" % enumeratumVersion
)

val json4s: Seq[ModuleID] = Seq(
  "org.json4s" %% "json4s-jackson" % json4sVersion
)

val logback: Seq[ModuleID] = Seq(
  "ch.qos.logback" % "logback-core"    % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

val monix: Seq[ModuleID] = Seq(
  "io.monix" %% "monix" % monixVersion
)

val nimbus: Seq[ModuleID] = Seq(
  "com.nimbusds" % "nimbus-jose-jwt" % nimbusVersion
)

val scalaMeter: Seq[ModuleID] = Seq(
  "com.storm-enroute" %% "scalameter" % scalaMeterVersion
)

val scalaTest: Seq[ModuleID] = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

val sttpAkka: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp" %% "akka-http-backend"              % sttpVersion,
  "com.softwaremill.sttp" %% "async-http-client-backend-cats" % sttpVersion,
  "com.softwaremill.sttp" %% "core"                           % sttpVersion,
  "com.softwaremill.sttp" %% "json4s"                         % sttpVersion,
  "com.typesafe.akka"     %% "akka-stream"                    % akkaStreamsVersion
)

val sttpMonix: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp" %% "async-http-client-backend-monix" % sttpVersion,
  "com.softwaremill.sttp" %% "core"                            % sttpVersion,
  "com.softwaremill.sttp" %% "json4s"                          % sttpVersion
)

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

logBuffered := false

parallelExecution in Test := false

// --------------------------------------------- //
// Project and configuration for keycloak4s-core //
// --------------------------------------------- //
lazy val coreDependencies: Seq[ModuleID] = cats ++ json4s ++ logback ++ enumeratum

lazy val `keycloak4s-core` = (project in file("./keycloak4s-core"))
  .settings(global: _*)
  .settings(libraryDependencies ++= coreDependencies)
  .settings(name := "keycloak4s-core", publishArtifact := true)

// ---------------------------------------------- //
// Project and configuration for keycloak4s-admin //
// ---------------------------------------------- //
lazy val adminDependencies: Seq[ModuleID] = sttpAkka

lazy val `keycloak4s-admin` = (project in file("./keycloak4s-admin"))
  .settings(global: _*)
  .settings(libraryDependencies ++= adminDependencies)
  .settings(name := "keycloak4s-admin", publishArtifact := true)
  .dependsOn(`keycloak4s-core`)

// ---------------------------------------------------- //
// Project and configuration for keycloak4s-admin-monix //
// ---------------------------------------------------- //
lazy val monixDependencies: Seq[ModuleID] = sttpMonix ++ monix

lazy val `keycloak4s-monix` = (project in file("./keycloak4s-admin-monix"))
  .settings(global: _*)
  .settings(libraryDependencies ++= monixDependencies)
  .settings(name := "keycloak4s-admin-monix", publishArtifact := true)
  .dependsOn(`keycloak4s-admin`)

// ------------------------------------------------------- //
// Project and configuration for keycloak4s-auth-akka-http //
// ------------------------------------------------------- //
lazy val akkaHttpDependencies: Seq[ModuleID] = akkaHttp ++ nimbus

lazy val `keycloak4s-akka-http` = (project in file("./keycloak4s-auth/akka-http"))
  .settings(global: _*)
  .settings(libraryDependencies ++= akkaHttpDependencies)
  .settings(name := "keycloak4s-auth-akka-http", publishArtifact := true)
  .dependsOn(`keycloak4s-core`)

// --------------------------------------------------- //
// Project and configuration for keycloak4s-playground //
// --------------------------------------------------- //
lazy val playgroundDependencies: Seq[ModuleID] = scalaTest ++ scalaMeter

lazy val `keycloak4s-playground` = (project in file("./keycloak4s-playground"))
  .settings(global: _*)
  .settings(libraryDependencies ++= playgroundDependencies)
  .settings(name := "keycloak4s-playground", publishArtifact := true)
  .dependsOn(`keycloak4s-admin`, `keycloak4s-monix`, `keycloak4s-akka-http`)

// ---------------------------------------------- //
// Project and configuration for the root project //
// ---------------------------------------------- //
lazy val root = (project in file("."))
  .settings(global: _*)
  .settings(publishArtifact := false)
  .aggregate(
    `keycloak4s-core`,
    `keycloak4s-admin`,
    `keycloak4s-monix`,
    `keycloak4s-akka-http`,
    `keycloak4s-playground`
  )