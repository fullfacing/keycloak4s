import sbt.Keys.{credentials, publishMavenStyle}
import ReleaseTransformations._
import sbt.{Credentials, url}
import xerial.sbt.Sonatype.GitHubHosting

val baseScalaOpts = Seq(
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

val scalac213Opts = baseScalaOpts
val scalac212Opts = baseScalaOpts ++ Seq("-Ypartial-unification")

lazy val global = {
  Seq(
    scalaVersion  := "2.13.1",
    organization  := "com.fullfacing",
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n <= 12 => scalac212Opts
      case _                       => scalac213Opts
    }),

    credentials += Credentials("GnuPG Key ID", "gpg", "419C90FB607D11B0A7FE51CFDAF842ABC601C14F", "ignored"),

    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary,

    crossScalaVersions := Seq(scalaVersion.value, "2.12.10"),

    // Your profile name of the sonatype account. The default is the same with the organization value
    sonatypeProfileName := "com.fullfacing",

    publishTo := sonatypePublishToBundle.value,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),

    // To sync with Maven central, you need to supply the following information:
    publishMavenStyle := true,

    // MIT Licence
    licenses  := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  
    // Github Project Information
    sonatypeProjectHosting := Some(GitHubHosting("fullfacing", "keycloak4s", "curious@fullfacing.com")),
  
    // Developer Contact Information
    developers := List(
      Developer(
        id    = "Executioner1939",
        name  = "Richard Peters",
        email = "rpeters@fullfacing.com",
        url   = url("https://www.fullfacing.com/")
      ),
      Developer(
        id    = "lmuller90",
        name  = "Louis Muller",
        email = "lmuller@fullfacing.com",
        url   = url("https://www.fullfacing.com/")
      ),
      Developer(
        id    = "StuartJ45",
        name  = "Stuart Jameson",
        email = "sjameson@fullfacing.com",
        url   = url("https://www.fullfacing.com/")
      ),
      Developer(
        id    = "neil-fladderak",
        name  = "Neil Fladderak",
        email = "neil@fullfacing.com",
        url   = url("https://www.fullfacing.com/")
      )
    ),

    releaseIgnoreUntrackedFiles := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseCrossBuild := true,
    releaseVersionBump := sbtrelease.Version.Bump.Minor,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      setReleaseVersion,
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
    )
  )
}

// ---------------------------------- //
//          Library Versions          //
// ---------------------------------- //
val akkaHttpVersion       = "10.1.11"
val akkaStreamsVersion    = "2.6.4"
val catsEffectVersion     = "2.1.3"
val catsCoreVersion       = "2.1.1"
val enumeratumVersion     = "1.5.15"
val json4sVersion         = "3.6.7"
val logbackVersion        = "1.2.3"
val monixVersion          = "3.1.0"
val nimbusVersion         = "8.15"
val scalaTestVersion      = "3.1.1"
val sttpVersion           = "2.0.9"

// -------------------------------------- //
//          Library Dependencies          //
// -------------------------------------- //
val akkaHttp: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-stream"  % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http"    % akkaHttpVersion
)

val akkaTestKit: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamsVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
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

val scalaTest: Seq[ModuleID] = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

val sttp: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp.client" %% "core"   % sttpVersion,
  "com.softwaremill.sttp.client" %% "json4s" % sttpVersion
)

val sttpAkka: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp.client" %% "akka-http-backend" % sttpVersion
)

val sttpAkkaMonix: Seq[ModuleID] = Seq(
  "com.fullfacing" %% "sttp-akka-monix" % "1.1.0"
)

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
lazy val `keycloak4s-admin` = (project in file("./keycloak4s-admin"))
  .settings(global: _*)
  .settings(libraryDependencies ++= sttp)
  .settings(name := "keycloak4s-admin", publishArtifact := true)
  .dependsOn(`keycloak4s-core`)

// ---------------------------------------------------- //
// Project and configuration for keycloak4s-admin-monix //
// ---------------------------------------------------- //
lazy val `keycloak4s-monix` = (project in file("./keycloak4s-admin-monix"))
  .settings(global: _*)
  .settings(libraryDependencies ++= monix)
  .settings(name := "keycloak4s-admin-monix", publishArtifact := true)
  .dependsOn(`keycloak4s-admin`)

// ------------------------------------------------------- //
// Project and configuration for keycloak4s-auth-core //
// ------------------------------------------------------- //
lazy val `keycloak4s-auth-core` = (project in file("./keycloak4s-auth/core"))
  .settings(global: _*)
  .settings(libraryDependencies ++= nimbus)
  .settings(name := "keycloak4s-auth-core", publishArtifact := true)
  .dependsOn(`keycloak4s-core`)

// ------------------------------------------------------- //
// Project and configuration for keycloak4s-auth-akka-http //
// ------------------------------------------------------- //
lazy val `keycloak4s-akka-http` = (project in file("./keycloak4s-auth/akka-http"))
  .settings(global: _*)
  .settings(libraryDependencies ++= akkaHttp)
  .settings(name := "keycloak4s-auth-akka-http", publishArtifact := true)
  .dependsOn(`keycloak4s-auth-core`)

// --------------------------------------------------- //
// Project and configuration for keycloak4s-playground //
// --------------------------------------------------- //
lazy val `keycloak4s-playground` = (project in file("./keycloak4s-playground"))
  .settings(scalaVersion  := "2.13.1")
  .settings(skip in publish := true)
  .settings(libraryDependencies ++= sttpAkkaMonix ++ scalaTest ++ akkaTestKit ++ sttpAkka)
  .settings(coverageEnabled := false)
  .settings(parallelExecution in Test := false)
  .settings(scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n <= 12 => scalac212Opts
    case _                       => scalac213Opts
  }))
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
  .settings(name := "keycloak4s-playground", publishArtifact := false)
  .dependsOn(`keycloak4s-admin`, `keycloak4s-monix`, `keycloak4s-akka-http`)

// ---------------------------------------------- //
// Project and configuration for the root project //
// ---------------------------------------------- //
lazy val root = (project in file("."))
  .settings(global: _*)
  .settings(publishArtifact := false)
  .settings(skip in publish := true)
  .aggregate(
    `keycloak4s-core`,
    `keycloak4s-admin`,
    `keycloak4s-monix`,
    `keycloak4s-auth-core`,
    `keycloak4s-akka-http`,
    `keycloak4s-playground`
  )
