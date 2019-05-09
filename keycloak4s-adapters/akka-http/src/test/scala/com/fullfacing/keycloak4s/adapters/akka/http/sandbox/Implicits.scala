package com.fullfacing.keycloak4s.adapters.akka.http.sandbox

import com.fullfacing.keycloak4s.adapters.akka.http.models.SecurityConfig
import com.fullfacing.keycloak4s.adapters.akka.http.services.TokenValidator
import monix.execution.Scheduler

object Implicits {
  implicit val scheduler: Scheduler = Scheduler.io("adaptor-test-io")
  implicit val tv: TokenValidator = new TokenValidator("localhost", "8080", "Retry")


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
  import com.fullfacing.keycloak4s.client.serialization.JsonFormats.default
  val apiSecurityConfig: SecurityConfig = read[SecurityConfig](configString)
}
