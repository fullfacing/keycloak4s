package com.fullfacing.keycloak4s

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

package object services {
  def createQuery(queries: (String, Option[Any])*): Seq[KeyValue] = {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }
  }
}
