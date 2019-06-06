package com.fullfacing.keycloak4s.benchmarks

import java.util.UUID

import com.fullfacing.keycloak4s.auth.akka.http.services.TokenValidator
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.{JWK, JWKSet}
import com.nimbusds.jwt.SignedJWT
import org.scalameter
import org.scalameter.api._
import org.scalameter.{Aggregator, Gen, api}
import org.scalameter.api.Bench
import org.scalameter.picklers.Implicits._

object ValidationBenchmark extends Bench.ForkedTime {
  override def aggregator: Aggregator[Double] = Aggregator.average

  implicit val cId: UUID = UUID.randomUUID()

  val validator = TokenValidator("localhost", "8088", "test")

  //val jwk: JWK = JWK.parse("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxUtcfx+rgW77RpStefhJpnANi2eea2jaREj3m9q2gxcBKU6Bfskii/fZwYx5+oNaMMaTmXXIWUGozB7ioeqB89TAu4F3/psaS00rUQbwRdgtLwhiX03m0Ao2l9qGDYynTnxdFUdUVWRYRDLGwt2Tl8diBVyxWPvhk00CvL3T2CJEdmeoe2nbAWlz/MDXogknj7wI4npTC33MArDp92Wus2w9DO4zPv76jO9VnUDyutHaCqPVXQHcNuh/JWDCLi6DxbNRa9Om7okU/q9L7uM1oHdgeFNbYVG4zpLLyeuPyEZoK/1KRMX2uRP0c3/nZlMP312Wnc4RawewE9Bht/JsLQIDAQAB")

  val accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI1MmV1cUJseTVlV3hyNUxBdnppdDRGcUlPOU5saUNtZW9NLXp6S09JWURvIn0.eyJqdGkiOiJlNGI4NDc2Ny0zMzFlLTQ3ZDktODA4YS04ZDY4ZGI1ZDFjYTMiLCJleHAiOjE1NTk3NDkzNjEsIm5iZiI6MCwiaWF0IjoxNTU5NzMxMzYxLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODgvYXV0aC9yZWFsbXMvdGVzdCIsImF1ZCI6WyJyZWFsbS1tYW5hZ2VtZW50IiwiZm5nLWFwaS10ZXN0IiwiYWNjb3VudCJdLCJzdWIiOiIyM2Y2OTU3MS04MDEzLTRmNGUtODU3NS1hYjZlODRhN2YyYzkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJmcm9udGVuZCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjJjMTU3NzczLTVkMzUtNDEzOS04YjY1LWQ2ZmQ1YzM4YzNiOSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InJlYWxtLW1hbmFnZW1lbnQiOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJyZWFsbS1hZG1pbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJxdWVyeS1yZWFsbXMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJxdWVyeS1jbGllbnRzIiwicXVlcnktdXNlcnMiLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyIsInF1ZXJ5LWdyb3VwcyJdfSwiZm5nLWFwaS10ZXN0Ijp7InJvbGVzIjpbImNhcnMtdmlldyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoidGVzdCB1c2VyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdF91c2VyIiwiZ2l2ZW5fbmFtZSI6InRlc3QiLCJmYW1pbHlfbmFtZSI6InVzZXIiLCJlbWFpbCI6InRlc3RfdXNlckBrZXljbG9hay5jb20ifQ.Xd9gSHGCUXS98l0vvpJqF2BLy0BSqNACQDApajcNrZfZbY7_0TG7gKvvS7KCOpOQGt2Lq5vT2RNXbi6hLHMVPpuqyfO4YXotdJXWCQzPpEpXw-0xe4UIyDzbl7E-QdDfDCRLihlzw5TwHfaixm9awr3Y44C4gU_PJZUbnfaThRLwGTf7HuvTZLG-j3LCHK9BOCKqnTxoeXM9VfNnREAUoy0nrmz5uqU-eiRLEAADACZaH4K5XPtTSir_EXn_qL2l-iGFzeCJPl2beClojoBHcl50VifPDJVAPbsQkNXzSyK460wQ9e9otDcr4MQVXXqE3sUhHVR1DXPeamTYalSNfg"

  val idToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI1MmV1cUJseTVlV3hyNUxBdnppdDRGcUlPOU5saUNtZW9NLXp6S09JWURvIn0.eyJqdGkiOiJhY2NkZGM1OC1kNmY4LTQxYTQtYjQ4OS0xY2QyNGU0ZDliNGQiLCJleHAiOjE1NTk3NDkzNjEsIm5iZiI6MCwiaWF0IjoxNTU5NzMxMzYxLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODgvYXV0aC9yZWFsbXMvdGVzdCIsImF1ZCI6ImZyb250ZW5kIiwic3ViIjoiMjNmNjk1NzEtODAxMy00ZjRlLTg1NzUtYWI2ZTg0YTdmMmM5IiwidHlwIjoiSUQiLCJhenAiOiJmcm9udGVuZCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjJjMTU3NzczLTVkMzUtNDEzOS04YjY1LWQ2ZmQ1YzM4YzNiOSIsImFjciI6IjEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6InRlc3QgdXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6InRlc3RfdXNlciIsImdpdmVuX25hbWUiOiJ0ZXN0IiwiZmFtaWx5X25hbWUiOiJ1c2VyIiwiZW1haWwiOiJ0ZXN0X3VzZXJAa2V5Y2xvYWsuY29tIn0.iNFDu_8_56LIu-3czyAd93T2tz9z7utHR_Nswt7pRwl-eGS8YogUh4bNsjKuYTCZk2sd8Blg5QZVLLY2XNy4dm9PQVpXLZqWBzKayRbSGXFF5js-zwyGYg6vthETciBv-e5RWcitGnDm4fUVpNS9eBv1YQ-gRg_hB5AqrfJu_p-i2_NkfJgqxMJ3TEBQWk2jYg1yNllCU9V9FGaNWoQn9C0xePMPeMRhO41jYpArKWFiK1C54fncXve9zKCuho3F706OpjBhNY8u2fJ_hkRx14ZstEop6VrUWjx6seR-bogF0y1tBPzImPlWucnVEMdqYaechgkjoIPx-pr1GmsZAA"

  val range: Gen[Int] = Gen.range("size")(1, 10, 1)

  val keySet: JWKSet = JWKSet.parse("""{"keys":[{"kty":"RSA","x5t#S256":"k2g-vNcsY2ymV8f8L0MOwL5OM6EZ82weEdlB_velSzA","e":"AQAB","use":"sig","x5t":"6Gx76rtxDXDpunUcBjxJdmjltBk","kid":"52euqBly5eWxr5LAvzit4FqIO9NliCmeoM-zzKOIYDo","x5c":["MIIClzCCAX8CBgFq1Va7CDANBgkqhkiG9w0BAQsFADAPMQ0wCwYDVQQDDAR0ZXN0MB4XDTE5MDUyMDEzMDEzMloXDTI5MDUyMDEzMDMxMlowDzENMAsGA1UEAwwEdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMVLXH8fq4Fu+0aUrXn4SaZwDYtnnmto2kRI95vatoMXASlOgX7JIov32cGMefqDWjDGk5l1yFlBqMwe4qHqgfPUwLuBd\/6bGktNK1EG8EXYLS8IYl9N5tAKNpfahg2Mp058XRVHVFVkWEQyxsLdk5fHYgVcsVj74ZNNAry909giRHZnqHtp2wFpc\/zA16IJJ4+8COJ6Uwt9zAKw6fdlrrNsPQzuMz7++ozvVZ1A8rrR2gqj1V0B3DbofyVgwi4ug8WzUWvTpu6JFP6vS+7jNaB3YHhTW2FRuM6Sy8nrj8hGaCv9SkTF9rkT9HN\/52ZTD99dlp3OEWsHsBPQYbfybC0CAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAioVRTop+vlfX9GRRVlj6reMsrOvfmP8KRJX8YvDuOUanAHQzDt+7JK0lWeQnJOMpkk21S0mi2yKuepMmpEQ6F\/X40Uafon\/tThTXf4V4wOPnVyW190iU1LUoJXV8WH+QvMULqhxFC6tKfkpXJRokd4MBgyL5tXzHDOI+ueqFrYwruzXi39mxyebsvfNYufuq+SXxkZYynei+oEYFb7AaTBPcY30MY7RjlfPFMkds9XlxpSmgZV4+AJMN7V1Y4XUpO\/NSBl7Qj2LJSV7tBaVMBWfDZcJI0Jug6aYpqZ2YbtMzoyfisG9E65fvhCydbixJMyrxHBDr5SUiAJQ\/wjF33Q=="],"alg":"RS256","n":"xUtcfx-rgW77RpStefhJpnANi2eea2jaREj3m9q2gxcBKU6Bfskii_fZwYx5-oNaMMaTmXXIWUGozB7ioeqB89TAu4F3_psaS00rUQbwRdgtLwhiX03m0Ao2l9qGDYynTnxdFUdUVWRYRDLGwt2Tl8diBVyxWPvhk00CvL3T2CJEdmeoe2nbAWlz_MDXogknj7wI4npTC33MArDp92Wus2w9DO4zPv76jO9VnUDyutHaCqPVXQHcNuh_JWDCLi6DxbNRa9Om7okU_q9L7uM1oHdgeFNbYVG4zpLLyeuPyEZoK_1KRMX2uRP0c3_nZlMP312Wnc4RawewE9Bht_JsLQ"}]}""")

  import com.nimbusds.jwt.SignedJWT.parse

  val aT: SignedJWT = parse(accessToken)
  val iT: SignedJWT = parse(idToken)

  val keyId: String = aT.getHeader.getKeyID

  val verifier: RSASSAVerifier = validator.createRsaVerifier(keyId, keySet).unsafeRunSync().right.get

  performance of "TokenValidator" in {
    measure method "validateClaims" config (
      exec.benchRuns -> 64
      ) in {
      using(Gen.unit("once")) in { _ =>
        validator.validateClaims(aT, Some(iT))
      }
    }

    /*measure method "parseTokens" config (
      exec.benchRuns -> 64
    ) in {
      using(Gen.unit("once")) in { _ =>
        validator.parseTokens(accessToken, Some(idToken)).unsafeRunSync()
      }
    }

    measure method "validateTimes" config (
      exec.benchRuns -> 64
    ) in {
      using(Gen.unit("once")) in { _ =>
        validator.validateTimes(aT, Some(iT))
      }
    }*/
  }



//  performance of "TokenValidator" in {
//    measure method "validate" config (
//      exec.benchRuns -> 64
//    ) in {
//      using(range) in { _ =>
//        validator.validate(accessToken, Some(idToken)).unsafeRunSync()
//      }
//    }
//  }

}
