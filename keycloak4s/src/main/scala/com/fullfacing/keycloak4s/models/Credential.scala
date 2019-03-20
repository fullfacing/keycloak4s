package com.fullfacing.keycloak4s.models

case class Credential(algorithm: Option[String],
                      config: Option[MultivaluedHashMap],
                      counter: Option[Int],
                      createdDate: Option[Long],
                      device: Option[String],
                      digits: Option[Int],
                      hashIterations: Option[Int],
                      hashedSaltedValue: Option[String],
                      period: Option[Int],
                      salt: Option[String],
                      temporary: Option[Boolean],
                      `type`: Option[String],
                      value: Option[String])
