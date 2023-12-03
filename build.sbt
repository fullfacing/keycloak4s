import sbt.Keys.{credentials, publishMavenStyle}
import ReleaseTransformations._
import sbt.{Credentials, url}
import sbtrelease.Version.Bump.Bugfix
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
    scalaVersion  := "2.13.10",
    organization  := "com.fullfacing",
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n <= 12 => scalac212Opts
      case _                       => scalac213Opts
    }),

    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),

    credentials += Credentials("GnuPG Key ID", "gpg", "B45D4204DBB121424926CFA6DBC0CB15C9B7283D", "ignored"),

    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary,

    crossScalaVersions := Seq(scalaVersion.value, "2.12.17"),

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

    releaseCommitMessage := s"[skip ci] Setting version to ${(ThisBuild / version).value}",
    releaseNextCommitMessage := s"[skip ci] Setting version to ${(ThisBuild / version).value}",
    releaseIgnoreUntrackedFiles := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseCrossBuild := true,
    releaseVersionBump := Bugfix,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      setReleaseVersion,
      pushChanges,
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease")
    )
  )
}

// ---------------------------------- //
//          Library Versions          //
// ---------------------------------- //
val akkaHttpVersion       = "10.5.0"
val akkaStreamsVersion    = "2.7.0"
val catsEffectVersion     = "3.4.8"
val catsCoreVersion       = "2.9.0"
val enumeratumVersion     = "1.7.2"
val json4sVersion         = "4.0.7"
val logbackVersion        = "1.4.5"
val nimbusVersion         = "9.30.2"
val scalaTestVersion      = "3.2.15"
val sttpVersion           = "3.8.13"

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

val nimbus: Seq[ModuleID] = Seq(
  "com.nimbusds" % "nimbus-jose-jwt" % nimbusVersion,
  "net.minidev" % "json-smart" % "2.4.9"
)

val scalaTest: Seq[ModuleID] = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

val sttp: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp.client3" %% "core"   % sttpVersion,
  "com.softwaremill.sttp.client3" %% "json4s" % sttpVersion
)


// --------------------------------------------- //
// Project and configuration for keycloak4s-core //
// --------------------------------------------- //
lazy val coreDependencies: Seq[ModuleID] = cats ++ json4s ++ logback ++ enumeratum

lazy val `keycloak4s-core` = (project in file("./keycloak4s-core"))
  .settings(global: _*)
  .settings(libraryDependencies ++= coreDependencies)
  .settings(name := "keycloak4s-core-ce3", publishArtifact := true)

// ---------------------------------------------- //
// Project and configuration for keycloak4s-admin //
// ---------------------------------------------- //
lazy val `keycloak4s-admin` = (project in file("./keycloak4s-admin"))
  .settings(global: _*)
  .settings(libraryDependencies ++= sttp)
  .settings(name := "keycloak4s-admin-ce3", publishArtifact := true)
  .dependsOn(`keycloak4s-core`)

// --------------------------------------------------- //
// Project and configuration for keycloak4s-playground //
// --------------------------------------------------- //
val catsBackend: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp.client3" %% "armeria-backend-cats" % "3.8.13"
)

 lazy val `keycloak4s-playground` = (project in file("./keycloak4s-playground"))
   .settings(scalaVersion  := "2.13.8")
   .settings(publish / skip := true)
   .settings(libraryDependencies ++= scalaTest ++ catsBackend)
   .settings(coverageEnabled := false)
   .settings(Test / parallelExecution := false)
   .settings(scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
     case Some((2, n)) if n <= 12 => scalac212Opts
     case _                       => scalac213Opts
   }))
   .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
   .settings(name := "keycloak4s-playground", publishArtifact := false)
   .dependsOn(`keycloak4s-admin`)

// ---------------------------------------------- //
// Project and configuration for the root project //
// ---------------------------------------------- //
lazy val root = (project in file("."))
  .settings(global: _*)
  .settings(publishArtifact := false)
  .settings(publish / skip := true)
  .aggregate(
    `keycloak4s-core`,
    `keycloak4s-admin`,
    `keycloak4s-playground`
  )
