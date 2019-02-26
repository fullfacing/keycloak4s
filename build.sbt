name := "keycloak4s"

version := "0.1"

scalaVersion := "2.12.8"

resolvers ++= {
  val nexusURL = sys.env.getOrElse("NEXUS_REPO_URL", "nexus.k8s.dev.fin.fullfacing.com")
  Seq(
    "Sonatype OSS Releases" at s"https://$nexusURL/repository/maven-releases",
    "Sonatype OSS Snapshots" at s"https://$nexusURL/repository/maven-snapshots"
  )
}

val keycloak = {
  val version = "4.8.2.Final"
  Seq(
    "org.keycloak" % "keycloak-authz-client" % version,
    "org.keycloak" % "keycloak-admin-client" % version
  )
}

val enumeratum_Json4s = {
  val version ="1.5.13"
  Seq("com.beachape" %% "enumeratum" % version)
}

val apollo = {
  val version = "2.1.2-SNAPSHOT"
  Seq("com.fullfacing" %% "apollo-core" % version)
}

val sttp = {
  val version = "1.5.11"
  Seq(
    "com.softwaremill.sttp" %% "core" % version,
    "com.softwaremill.sttp" %% "async-http-client-backend-monix" % version
  )
}

libraryDependencies := keycloak ++ enumeratum_Json4s ++ apollo ++ sttp

val scalacOpts = Seq(
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ypartial-unification",
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole,
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

scalacOptions ++= scalacOpts