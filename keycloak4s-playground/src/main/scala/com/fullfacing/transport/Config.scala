package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.akka.http.models.SecurityConfig

object Config {
  val configString: String =
    """
      {
        "service": "fng-api-sensorthings",
        "nodes": [
          {
            "resource": "things",
            "nodes": [
              {
                "resource": "datastreams",
                "nodes": []
              }
            ]
          },
          {
            "resource": "datastreams",
            "nodes": [
              {
                "resource": "observations",
                "nodes": []
              }
            ]
          },
          {
            "resource": "observations",
            "nodes": [
              {
                "resource": "featureofinterests",
                "nodes": []
              }
            ]
          },
          {
            "resource": "sensors",
            "nodes": [
              {
                "resource": "datastreams",
                "nodes": []
              }
            ]
          }
        ]
      }
    """

  import org.json4s.jackson.Serialization.read
  import com.fullfacing.keycloak4s.core.serialization.JsonFormats.default
  val apiSecurityConfig: SecurityConfig = read[SecurityConfig](configString)
}
