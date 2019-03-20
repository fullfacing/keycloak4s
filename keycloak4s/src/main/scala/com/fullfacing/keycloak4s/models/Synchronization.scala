package com.fullfacing.keycloak4s.models

case class Synchronization(added: Option[Int],
                           failed: Option[Int],
                           ignored: Option[Boolean],
                           removed: Option[Int],
                           status: Option[String],
                           updated: Option[Int])
