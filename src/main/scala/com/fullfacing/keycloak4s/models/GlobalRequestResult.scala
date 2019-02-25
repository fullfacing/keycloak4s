package com.fullfacing.keycloak4s.models

case class GlobalRequestResult(
                                failedRequests: Option[List[String]],
                                successRequests: Option[List[String]]
                              )
