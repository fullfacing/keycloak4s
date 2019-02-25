package com.fullfacing.keycloak4s.models

case class MultivaluedHashMap(
                               empty: Option[Boolean],
                               loadFactor: Option[Float],
                               threshold: Option[Int]
                             )
