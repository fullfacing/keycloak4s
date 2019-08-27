package com.fullfacing.transport

import com.fullfacing.keycloak4s.auth.core.validation.TokenValidator
import monix.execution.Scheduler

object Implicits {
  implicit val scheduler: Scheduler = Scheduler.io("io")
  implicit val tv: TokenValidator = TokenValidator.Dynamic(Main.config)
}
