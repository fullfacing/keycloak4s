package com.fullfacing.keycloak4s.models

//IMPORTANT! final case class is not concrete, as it was not included in documentation and was determined from request/response testing.
final case class BruteForceResponse(numFailures: Int,
                                    disabled: Boolean,
                                    lastIPFailure: String,
                                    lastFailure: Int)
