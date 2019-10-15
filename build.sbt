import sbt.Keys.{credentials, publishMavenStyle}
import sbt.{Credentials, url}
import xerial.sbt.Sonatype.GitHubHosting

lazy val global = {
  Seq(
    version       := "1.2.3",
    scalaVersion  := "2.13.0",
    organization  := "com.fullfacing",
    scalacOptions ++= scalacOpts,

    // Your profile name of the sonatype account. The default is the same with the organization value
    sonatypeProfileName := "com.fullfacing",

    // Sonatype Nexus Credentials
    credentials += Credentials(Path.userHome / ".sbt" / "1.0" / ".credentials"),

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
    )
  )
}

val scalacOpts = Seq(
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

// ---------------------------------- //
//          Library Versions          //
// ---------------------------------- //
val akkaHttpVersion       = "10.1.10"
val akkaStreamsVersion    = "2.5.25"
val catsEffectVersion     = "2.0.0"
val catsCoreVersion       = "2.0.0"
val enumeratumVersion     = "1.5.15"
val json4sVersion         = "3.6.7"
val logbackVersion        = "1.2.3"
val monixVersion          = "3.0.0"
val nimbusVersion         = "8.1"
val scalaTestVersion      = "3.0.8"
val sttpAkkaMonixVersion  = "1.0.2"
val sttpVersion           = "1.7.2"

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

val sttpAkka: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp" %% "akka-http-backend" % sttpVersion,
  "com.softwaremill.sttp" %% "core"              % sttpVersion,
  "com.softwaremill.sttp" %% "json4s"            % sttpVersion,
  "com.typesafe.akka"     %% "akka-stream"       % akkaStreamsVersion
)

val sttpAkkaMonix: Seq[ModuleID] = Seq(
  "com.fullfacing" %% "sttp-akka-monix" % sttpAkkaMonixVersion
)

val sttpMonix: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp" %% "core"   % sttpVersion,
  "com.softwaremill.sttp" %% "json4s" % sttpVersion
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
lazy val playgroundDependencies: Seq[ModuleID] = scalaTest ++ sttpAkkaMonix ++ akkaTestKit

lazy val `keycloak4s-playground` = (project in file("./keycloak4s-playground"))
  .settings(scalaVersion  := "2.13.0")
  .settings(libraryDependencies ++= playgroundDependencies)
  .settings(coverageEnabled := false)
  .settings(parallelExecution in Test := false)
  .settings(scalacOptions ++= scalacOpts)
  .settings(name := "keycloak4s-playground", publishArtifact := false)
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
    `keycloak4s-auth-core`,
    `keycloak4s-akka-http`,
    `keycloak4s-playground`
  )
