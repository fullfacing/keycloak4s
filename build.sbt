name         := "project-keycloak4s"
organization := "com.fullfacing"

lazy val global = {
  Seq(
    version       := "0.2.1-SNAPSHOT",
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

val logback: Seq[ModuleID] = {
  val version = "1.2.3"
  Seq(
    "ch.qos.logback" % "logback-core"    % version,
    "ch.qos.logback" % "logback-classic" % version
  )
}

val json4s: Seq[ModuleID] = {
  val version = "3.6.5"
  Seq(
    "org.json4s" %% "json4s-jackson" % version
  )
}

val sttp: Seq[ModuleID] = {
  val version = "1.5.11"
  Seq(
    "com.softwaremill.sttp" %% "core"              % version,
    "com.softwaremill.sttp" %% "akka-http-backend" % version,
    "com.typesafe.akka"     %% "akka-stream"       % "2.5.21",
    "com.softwaremill.sttp" %% "json4s"            % version
  )
}

val cats: Seq[ModuleID] = Seq(
  "org.typelevel" %% "cats-core"   % "1.6.0",
  "org.typelevel" %% "cats-effect" % "1.2.0"
)

val monix: Seq[ModuleID] = Seq(
  "io.monix" %% "monix" % "3.0.0-RC2"
)

// ----------------------------------------------- //
// Project and configuration for keycloak-monix    //
// ----------------------------------------------- //
lazy val `keycloak-dependencies`: Seq[ModuleID] = sttp ++ cats ++ json4s ++ logback ++ monix

lazy val keycloak4s = (project in file("./keycloak4s"))
  .settings(global: _*)
  .settings(libraryDependencies ++= `keycloak-dependencies`)
  .settings(name := "keycloak4s", publishArtifact := true)


// ----------------------------------------------- //
// Project and configuration for keycloak-monix    //
// ----------------------------------------------- //
lazy val `keycloak-monix-dependencies`: Seq[ModuleID] = monix

lazy val `keycloak4s-monix` = (project in file("./keycloak4s-monix"))
  .settings(global: _*)
  .settings(libraryDependencies ++= `keycloak-monix-dependencies`)
  .settings(name := "keycloak4s-monix", publishArtifact := true)
  .dependsOn(keycloak4s)


// ---------------------------------------------- //
// Project and configuration for the root project //
// ---------------------------------------------- //
lazy val root = (project in file("."))
  .settings(global: _*)
  .settings(publishArtifact := false)
  .aggregate(
    keycloak4s, 
    `keycloak4s-monix`
  )