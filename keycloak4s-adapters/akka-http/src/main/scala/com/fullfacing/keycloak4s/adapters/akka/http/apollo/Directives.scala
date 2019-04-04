package com.fullfacing.keycloak4s.adapters.akka.http.apollo

import com.fullfacing.apollo.http.directives.TaskDirectives

object Directives extends TaskDirectives
  with RequestContextDirectives
  with ValidationDirective