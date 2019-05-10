package com.fullfacing.keycloak4s.core.models

final case class GlobalRequestResult(failedRequests: Option[List[String]],
                                     successRequests: Option[List[String]])
