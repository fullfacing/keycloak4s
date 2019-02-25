name := "Keycloak"

version := "0.1"

scalaVersion := "2.12.8"

val keycloak = {
  val version = "4.8.2.Final"
  Seq(
    "org.keycloak" % "keycloak-authz-client" % version,
    "org.keycloak" % "keycloak-admin-client" % version
  )
}

val enumeratum_Json4s =  {
  val version ="1.5.13"
  Seq("com.beachape" %% "enumeratum" % version)
}

libraryDependencies := keycloak ++ enumeratum_Json4s