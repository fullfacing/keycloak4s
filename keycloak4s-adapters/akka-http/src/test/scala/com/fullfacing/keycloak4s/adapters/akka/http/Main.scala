package com.fullfacing.keycloak4s.adapters.akka.http

import cats.effect.ExitCode
import com.nimbusds.jose.jwk.RSAKey
import monix.eval.{Task, TaskApp}
import org.json4s.Formats
import org.json4s.jackson.Serialization.writePretty

object Main extends TaskApp {
  implicit val formats: Formats = org.json4s.DefaultFormats

  override def run(args: List[String]): Task[ExitCode] = {
    Task.eval {
      val rawToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJNVGQzNW5VSkEwQzRveGF0dldwR1NsWktNUmM2V01hWHF6ZTY2a21uQTJRIn0.eyJqdGkiOiI4MmFjNGQwZi01MDMzLTQzMzktODNjYS03MmVhMTJkMDA5ODMiLCJleHAiOjE1NTQzODA3OTAsIm5iZiI6MCwiaWF0IjoxNTU0MzgwNzMwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvbWFzdGVyIiwic3ViIjoiZGMwNzgzZjgtMzgxOS00Y2Q5LWEyMTQtZGFlYmQ3NjIzOGZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYWRtaW4tY2xpIiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiOTVmMmM3MzYtNGJjNy00N2RkLWJiM2UtOGRiYjhmZDUxMDQ5IiwiYWNyIjoiMSIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiY2xpZW50SG9zdCI6IjEyNy4wLjAuMSIsImNsaWVudElkIjoiYWRtaW4tY2xpIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LWFkbWluLWNsaSIsImNsaWVudEFkZHJlc3MiOiIxMjcuMC4wLjEiLCJlbWFpbCI6InNlcnZpY2UtYWNjb3VudC1hZG1pbi1jbGlAcGxhY2Vob2xkZXIub3JnIn0.d6JBdyQkcQudCtvddt0invmCECxl0WKIZ4civiaIwhROODBQVFL04rbrcs1SULJLhzY2plqMXicR5KI-EaeAlY7uEG5sP1VkqJxNy-gJfw0tehBx2DChg5hOiJtwDta12sX26vx444IS4Jadf_8-IO-jJ2z1oOHm2h9XQ8jlv5j-KP12a9-usIWZiv9sbFgQKZXfZDDNYDIszNYG_RXRaS0bDgwLtncxtXcTwbVsv2gJeU1fyVCY1WvzvbB4vWxxOWhndpasrqKIGK6CF1aKljwIOL1r7gctjOEAe2_BBedRws1L8ufj8tL2q3OVW_H8JrvfzdySrlec4LA3R-N0mg"

      val key = RSAKey.parse("""{
                               |            "kid": "MTd35nUJA0C4oxatvWpGSlZKMRc6WMaXqze66kmnA2Q",
                               |            "kty": "RSA",
                               |            "alg": "RS256",
                               |            "use": "sig",
                               |            "n": "rhHYr-ig5JAZ9MQM7TCYwd33M1VIDdzf-N4kRCkF6iL3bu0yjcel_WHvoz3X6bqFdXQ9EyHINxI-_DFGBZLuCYoe0xp4V4stQ68ATmd18Pw-k1kS1OHgHfEd6pZN-PS2RGgBQ5nuj44_vq-m3jSSDZrxHZ5CU06RrIBHxDiUpWKpBZDy-3oAT1EqDjOweFWm7farfHSf5iq4x87pworDQROhDZdUmfpgLlUQRdtj-d6aL9isscl2UcYyMAhHpdYzTc3XqY76hp7stmbndn7jvC6LZvuJh8SMgQhwgavrc36xDPIJ1SmhNXn4K4Pu1mXO65bctV5KeZgjQ_OnqXV_CQ",
                               |            "e": "AQAB"
                               |        }""".stripMargin)

      implicit val keys = List(key)

      //implicit val publicKeys: List[String] = List("MTd35nUJA0C4oxatvWpGSlZKMRc6WMaXqze66kmnA2Q")

      val token = TokenValidator.validate(rawToken)
      println("Token:\n" + token.map(_.toString))
      token.fold(_ => ExitCode.Error, _ => ExitCode.Success)
    }
  }.onErrorHandle{ex => ex.printStackTrace(); ExitCode.Error}
}
